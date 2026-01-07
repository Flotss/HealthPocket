package com.healthpocket.data.repository

import android.content.Context
import android.util.Log
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.notification.NotificationScheduler
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.MedicationIntakeRequest
import com.healthpocket.data.remote.dto.MedicationRequest
import com.healthpocket.data.remote.dto.MedicationResponse
import com.healthpocket.ui.theme.AppColors
import com.healthpocket.util.DateTimeUtils
import com.healthpocket.util.parseScheduleTime
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for medication data operations.
 * Implements offline-first pattern: writes to local DB, syncs to server when available.
 */
@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val intakeDao: MedicationIntakeDao,
    private val api: HealthPocketApi,
    @ApplicationContext private val context: Context
) {

    private val notificationScheduler by lazy { NotificationScheduler(context) }

    companion object {
        private const val DEFAULT_MEDICATION_DURATION_DAYS = 365
        private const val SCHEDULE_TIMES_SEPARATOR = ","
    }

    // ============================================================================
    // Public API - Medication Queries
    // ============================================================================

    fun getAllMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getAllMedications()
            .distinctUntilChanged()
    }

    fun getActiveMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getActiveMedications()
            .distinctUntilChanged()
    }

    fun getMedicationById(id: String): Flow<MedicationEntity?> {
        return medicationDao.getMedicationById(id)
            .distinctUntilChanged()
    }

    // ============================================================================
    // Public API - Medication Intake Queries
    // ============================================================================

    fun getIntakesForMedication(medicationId: String): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getIntakesForMedication(medicationId)
    }

    fun getIntakesInRange(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getIntakesInRange(startTime, endTime)
    }

    fun getTodaysPendingIntakes(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getTodaysPendingIntakes(startTime, endTime)
    }

    // ============================================================================
    // Public API - Medication Mutations
    // ============================================================================

    suspend fun createMedication(
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        notes: String? = null,
        color: String = AppColors.MEDICATION_DEFAULT,
        reminderEnabled: Boolean = true
    ): MedicationEntity {
        val medication = buildMedicationEntity(
            name = name,
            dosage = dosage,
            frequency = frequency,
            scheduleTimes = scheduleTimes,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            color = color,
            reminderEnabled = reminderEnabled
        )

        medicationDao.insert(medication)
        
        // Create intakes - wrap in try-catch to ensure medication is saved even if intake creation fails
        try {
            Log.d("MedicationRepository", "Creating intakes for medication ${medication.id} with ${scheduleTimes.size} schedule times")
            createIntakesForMedication(medication, scheduleTimes, startDate, endDate)
            Log.d("MedicationRepository", "Successfully created intakes for medication ${medication.id}")
            
            // Sync intakes to server
            syncIntakesForMedication(medication.id)
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Failed to create intakes for medication ${medication.id}", e)
            // Medication is already saved, intakes can be created later if needed
        }
        
        syncMedicationToServer(medication)

        return medication
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        val existingMedication = medicationDao.getMedicationByIdSync(medication.id)
        val updatedMedication = medication.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        medicationDao.update(updatedMedication)

        existingMedication?.let { existing ->
            if (hasScheduleTimesChanged(existing, updatedMedication)) {
                updateIntakesForScheduleChange(updatedMedication, existing)
            }
        }

        syncMedicationToServer(updatedMedication)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.delete(medication)
        medication.serverId?.let { serverId ->
            deleteMedicationFromServer(serverId)
        }
    }

    // ============================================================================
    // Public API - Medication Intake Mutations
    // ============================================================================

    suspend fun createIntake(
        medicationId: String,
        scheduledTime: Long
    ): MedicationIntakeEntity {
        val medication = medicationDao.getMedicationByIdSync(medicationId)
        val intake = MedicationIntakeEntity(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            scheduledTime = scheduledTime,
            status = IntakeStatus.PENDING,
            syncStatus = SyncStatus.PENDING
        )
        intakeDao.insert(intake)

        medication?.let {
            if (it.reminderEnabled) {
                notificationScheduler.scheduleIntakeReminder(intake.id, scheduledTime)
            }
        }

        syncIntakeToServer(intake)

        return intake
    }

    suspend fun markIntakeAsTaken(intake: MedicationIntakeEntity) {
        val takenTime = System.currentTimeMillis()
        intakeDao.updateIntakeStatus(
            id = intake.id,
            status = IntakeStatus.TAKEN,
            takenTime = takenTime,
            syncStatus = SyncStatus.PENDING
        )
        
        // Sync status change to server
        val updatedIntake = intake.copy(
            status = IntakeStatus.TAKEN,
            takenTime = takenTime,
            syncStatus = SyncStatus.PENDING
        )
        syncIntakeStatusToServer(updatedIntake)
    }

    suspend fun markIntakeAsSkipped(intake: MedicationIntakeEntity) {
        intakeDao.updateIntakeStatus(
            id = intake.id,
            status = IntakeStatus.SKIPPED,
            takenTime = null,
            syncStatus = SyncStatus.PENDING
        )
        notificationScheduler.cancelIntakeReminder(intake.id)
        
        // Sync status change to server
        val updatedIntake = intake.copy(
            status = IntakeStatus.SKIPPED,
            takenTime = null,
            syncStatus = SyncStatus.PENDING
        )
        syncIntakeStatusToServer(updatedIntake)
    }

    // ============================================================================
    // Public API - Sync Operations
    // ============================================================================

    suspend fun syncAll() {
        Log.d("MedicationRepository", "Starting bidirectional sync of medications and intakes")
        try {
            // Sync medications
            val serverMedications = api.getAllMedications().body() ?: emptyList()
            val localMedications = medicationDao.getAllMedicationsAsync()
            
            mergeAndSync(serverMedications, localMedications)
            
            // Sync all pending intakes
            syncAllPendingIntakes()
            
            Log.d("MedicationRepository", "Bidirectional sync completed successfully")
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Bidirectional sync failed", e)
        }
    }
    
    @Deprecated("Use syncAll() for bidirectional sync", ReplaceWith("syncAll()"))
    suspend fun syncMedications() {
        syncAll()
    }

    // ============================================================================
    // Private Helpers - Medication Entity Building
    // ============================================================================

    private fun buildMedicationEntity(
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate?,
        notes: String?,
        color: String,
        reminderEnabled: Boolean
    ): MedicationEntity {
        return MedicationEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            dosage = dosage,
            frequency = frequency,
            scheduleTimes = scheduleTimes.joinToString(SCHEDULE_TIMES_SEPARATOR),
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            color = color,
            reminderEnabled = reminderEnabled,
            isActive = true,
            syncStatus = SyncStatus.PENDING
        )
    }

    // ============================================================================
    // Private Helpers - Medication Sync
    // ============================================================================

    private suspend fun mergeAndSync(
        serverMedications: List<MedicationResponse>,
        localMedications: List<MedicationEntity>
    ) {
        val serverById = serverMedications.associateBy { it.id }
        val localByServerId = localMedications
            .filter { it.serverId != null }
            .associateBy { it.serverId!! }
        
        // Process server medications
        serverMedications.forEach { serverMedication ->
            val localMatch = localByServerId[serverMedication.id]
            
            when {
                localMatch == null -> {
                    handleNewServerMedication(serverMedication)
                }
                isServerNewer(serverMedication.updatedAt, localMatch.updatedAt) -> {
                    handleServerNewerMedication(serverMedication, localMatch)
                }
                isLocalNewer(serverMedication.updatedAt, localMatch.updatedAt) -> {
                    handleLocalNewerMedication(serverMedication, localMatch)
                }
                // If timestamps equal, no action needed
            }
        }
        
        // Process unsynchronized local medications
        val unsyncedLocalMedications = localMedications.filter { it.serverId == null }
        unsyncedLocalMedications.forEach { localMedication ->
            pushLocalMedicationToServer(localMedication)
        }
    }
    
    private suspend fun handleNewServerMedication(serverMedication: MedicationResponse) {
        Log.d("MedicationRepository", "Adding new medication from server: ${serverMedication.id}")
        val entity = serverMedication.toEntity()
        medicationDao.insert(entity)
    }
    
    private suspend fun handleServerNewerMedication(
        serverMedication: MedicationResponse,
        localMedication: MedicationEntity
    ) {
        Log.d("MedicationRepository", "Updating local medication from server: ${serverMedication.id}")
        val updatedEntity = serverMedication.toEntity(localId = localMedication.id)
        medicationDao.update(updatedEntity)
    }
    
    private suspend fun handleLocalNewerMedication(
        serverMedication: MedicationResponse,
        localMedication: MedicationEntity
    ) {
        Log.d("MedicationRepository", "Updating server with local medication: ${localMedication.id}")
        try {
            val request = localMedication.toRequest()
            val response = api.updateMedication(serverMedication.id, request)
            
            if (response.isSuccessful) {
                medicationDao.updateSyncStatus(localMedication.id, SyncStatus.SYNCED)
            } else {
                Log.e("MedicationRepository", "Failed to update server medication: ${response.code()}")
                medicationDao.updateSyncStatus(localMedication.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("MedicationRepository", "Network error updating medication to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }
    
    private suspend fun pushLocalMedicationToServer(localMedication: MedicationEntity) {
        Log.d("MedicationRepository", "Pushing local medication to server: ${localMedication.id}")
        try {
            val request = localMedication.toRequest()
            val response = api.createMedication(request)
            
            if (response.isSuccessful) {
                response.body()?.let { serverMedication ->
                    medicationDao.updateServerIdAndStatus(
                        localId = localMedication.id,
                        serverId = serverMedication.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                Log.e("MedicationRepository", "Failed to create medication on server: ${response.code()}")
                medicationDao.updateSyncStatus(localMedication.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("MedicationRepository", "Network error pushing medication to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }
    
    private fun isServerNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return serverMillis > localUpdatedAt
    }
    
    private fun isLocalNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return localUpdatedAt > serverMillis
    }

    private suspend fun syncMedicationToServer(medication: MedicationEntity) {
        Log.d("MedicationRepository", "Starting sync for medication ${medication.id}")
        try {
            val request = buildMedicationRequest(medication)
            val response = if (medication.serverId != null) {
                api.updateMedication(medication.serverId, request)
            } else {
                api.createMedication(request)
            }

            if (response.isSuccessful) {
                Log.d("MedicationRepository", "Sync successful for medication ${medication.id}")
                response.body()?.let { serverMedication ->
                    medicationDao.updateServerIdAndStatus(
                        localId = medication.id,
                        serverId = serverMedication.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                Log.e("MedicationRepository", "Sync failed for medication ${medication.id}: ${response.code()}")
                medicationDao.updateSyncStatus(medication.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Sync exception for medication ${medication.id}", e)
            medicationDao.updateSyncStatus(medication.id, SyncStatus.ERROR)
        }
    }

    private suspend fun deleteMedicationFromServer(serverId: String) {
        try {
            Log.d("MedicationRepository", "Syncing deletion for medication $serverId")
            api.deleteMedication(serverId)
            Log.d("MedicationRepository", "Deletion synced for medication $serverId")
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Deletion sync failed for medication $serverId", e)
            // Ignore network errors for deletion - offline-first pattern
        }
    }

    private suspend fun syncIntakeToServer(intake: MedicationIntakeEntity) {
        Log.d("MedicationRepository", "Starting sync for intake ${intake.id}")
        try {
            val request = MedicationIntakeRequest(
                medicationId = intake.medicationId,
                scheduledTime = DateTimeUtils.millisToOffsetDateTimeString(intake.scheduledTime),
                takenTime = intake.takenTime?.let { DateTimeUtils.millisToOffsetDateTimeString(it) },
                status = intake.status.name,
                notes = intake.notes,
                localId = intake.id
            )

            val response = api.createIntake(request)

            if (response.isSuccessful) {
                Log.d("MedicationRepository", "Sync successful for intake ${intake.id}")
                response.body()?.let { serverIntake ->
                    intakeDao.updateServerIdAndStatus(
                        localId = intake.id,
                        serverId = serverIntake.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                Log.e("MedicationRepository", "Sync failed for intake ${intake.id}: ${response.code()}")
                intakeDao.updateIntakeStatus(intake.id, intake.status, intake.takenTime, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("MedicationRepository", "Network error syncing intake to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }

    private suspend fun syncIntakeStatusToServer(intake: MedicationIntakeEntity) {
        Log.d("MedicationRepository", "Starting sync for intake status ${intake.id}")
        
        if (intake.serverId == null) {
            // If not synced yet, sync the entire intake
            syncIntakeToServer(intake)
            return
        }

        try {
            val request = com.healthpocket.data.remote.dto.UpdateIntakeStatusRequest(
                status = intake.status.name,
                takenTime = intake.takenTime?.let { DateTimeUtils.millisToOffsetDateTimeString(it) },
                notes = intake.notes
            )

            val response = api.updateIntakeStatus(intake.serverId, request)

            if (response.isSuccessful) {
                Log.d("MedicationRepository", "Status sync successful for intake ${intake.id}")
                intakeDao.updateIntakeStatus(intake.id, intake.status, intake.takenTime, SyncStatus.SYNCED)
            } else {
                Log.e("MedicationRepository", "Status sync failed for intake ${intake.id}: ${response.code()}")
                intakeDao.updateIntakeStatus(intake.id, intake.status, intake.takenTime, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("MedicationRepository", "Network error syncing intake status to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }

    private suspend fun syncIntakesForMedication(medicationId: String) {
        Log.d("MedicationRepository", "Starting sync of intakes for medication $medicationId")
        try {
            val pendingIntakes = intakeDao.getIntakesForMedication(medicationId).first()
                .filter { it.syncStatus == SyncStatus.PENDING && it.serverId == null }
            
            if (pendingIntakes.isEmpty()) {
                Log.d("MedicationRepository", "No pending intakes to sync for medication $medicationId")
                return
            }

            Log.d("MedicationRepository", "Syncing ${pendingIntakes.size} intakes for medication $medicationId")
            
            var syncedCount = 0
            var errorCount = 0
            
            pendingIntakes.forEach { intake ->
                try {
                    val request = MedicationIntakeRequest(
                        medicationId = intake.medicationId,
                        scheduledTime = DateTimeUtils.millisToOffsetDateTimeString(intake.scheduledTime),
                        takenTime = intake.takenTime?.let { DateTimeUtils.millisToOffsetDateTimeString(it) },
                        status = intake.status.name,
                        notes = intake.notes,
                        localId = intake.id
                    )

                    val response = api.createIntake(request)

                    if (response.isSuccessful) {
                        response.body()?.let { serverIntake ->
                            intakeDao.updateServerIdAndStatus(
                                localId = intake.id,
                                serverId = serverIntake.id,
                                status = SyncStatus.SYNCED
                            )
                            syncedCount++
                        }
                    } else {
                        errorCount++
                        intakeDao.updateIntakeStatus(intake.id, intake.status, intake.takenTime, SyncStatus.ERROR)
                    }
                } catch (e: Exception) {
                    errorCount++
                    Log.w("MedicationRepository", "Failed to sync intake ${intake.id}: ${e.message}")
                }
            }

            Log.d("MedicationRepository", "Synced $syncedCount intakes ($errorCount errors) for medication $medicationId")
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Failed to sync intakes for medication $medicationId", e)
        }
    }

    private suspend fun syncAllPendingIntakes() {
        Log.d("MedicationRepository", "Starting sync of all pending intakes")
        try {
            val pendingIntakes = intakeDao.getIntakesBySyncStatus(SyncStatus.PENDING)
                .filter { it.serverId == null }
            
            if (pendingIntakes.isEmpty()) {
                Log.d("MedicationRepository", "No pending intakes to sync")
                return
            }

            Log.d("MedicationRepository", "Syncing ${pendingIntakes.size} pending intakes")
            
            var syncedCount = 0
            var errorCount = 0
            
            pendingIntakes.forEach { intake ->
                try {
                    val request = MedicationIntakeRequest(
                        medicationId = intake.medicationId,
                        scheduledTime = DateTimeUtils.millisToOffsetDateTimeString(intake.scheduledTime),
                        takenTime = intake.takenTime?.let { DateTimeUtils.millisToOffsetDateTimeString(it) },
                        status = intake.status.name,
                        notes = intake.notes,
                        localId = intake.id
                    )

                    val response = api.createIntake(request)

                    if (response.isSuccessful) {
                        response.body()?.let { serverIntake ->
                            intakeDao.updateServerIdAndStatus(
                                localId = intake.id,
                                serverId = serverIntake.id,
                                status = SyncStatus.SYNCED
                            )
                            syncedCount++
                        }
                    } else {
                        errorCount++
                        intakeDao.updateIntakeStatus(intake.id, intake.status, intake.takenTime, SyncStatus.ERROR)
                    }
                } catch (e: Exception) {
                    errorCount++
                    // Keep PENDING status for retry when network returns
                }
            }

            Log.d("MedicationRepository", "Synced $syncedCount pending intakes ($errorCount errors)")
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Failed to sync pending intakes", e)
        }
    }

    suspend fun getPendingIntakesCount(): Int {
        return intakeDao.getIntakesBySyncStatus(SyncStatus.PENDING).size
    }

    private suspend fun syncServerMedicationToLocal(serverMedication: MedicationResponse) {
        val existingByServerId = medicationDao.getMedicationByServerId(serverMedication.id)

        when {
            existingByServerId != null -> {
                updateExistingMedicationFromServer(existingByServerId, serverMedication)
            }
            else -> {
                linkOrCreateLocalMedication(serverMedication)
            }
        }
    }

    private suspend fun updateExistingMedicationFromServer(
        existing: MedicationEntity,
        serverMedication: MedicationResponse
    ) {
        val updated = mapServerMedicationToEntity(
            serverMedication = serverMedication,
            localId = existing.id,
            serverId = existing.serverId
        )
        medicationDao.update(updated)
    }

    private suspend fun linkOrCreateLocalMedication(serverMedication: MedicationResponse) {
        val allMedications = medicationDao.getAllMedications().first()
        val existingLocal = findMatchingLocalMedication(allMedications, serverMedication)

        if (existingLocal != null) {
            linkLocalMedicationToServer(existingLocal, serverMedication)
        } else {
            createNewMedicationFromServer(serverMedication)
        }
    }

    private suspend fun linkLocalMedicationToServer(
        local: MedicationEntity,
        serverMedication: MedicationResponse
    ) {
        medicationDao.updateServerIdAndStatus(
            localId = local.id,
            serverId = serverMedication.id,
            status = SyncStatus.SYNCED
        )

        val updated = mapServerMedicationToEntity(
            serverMedication = serverMedication,
            localId = local.id,
            serverId = serverMedication.id
        )
        medicationDao.update(updated)
    }

    private suspend fun createNewMedicationFromServer(serverMedication: MedicationResponse) {
        val entity = mapServerMedicationToEntity(
            serverMedication = serverMedication,
            localId = UUID.randomUUID().toString(),
            serverId = serverMedication.id
        )
        medicationDao.insert(entity)
    }

    // ============================================================================
    // Private Helpers - Medication Mappers
    // ============================================================================

    private fun MedicationResponse.toEntity(
        localId: String = UUID.randomUUID().toString()
    ): MedicationEntity {
        return MedicationEntity(
            id = localId,
            serverId = this.id,
            name = this.name,
            dosage = this.dosage,
            frequency = this.frequency,
            scheduleTimes = this.scheduleTimes.joinToString(SCHEDULE_TIMES_SEPARATOR),
            startDate = LocalDate.parse(this.startDate),
            endDate = this.endDate?.let { LocalDate.parse(it) },
            notes = this.notes,
            color = this.color,
            reminderEnabled = this.reminderEnabled,
            isActive = this.isActive,
            syncStatus = SyncStatus.SYNCED,
            updatedAt = DateTimeUtils.offsetDateTimeStringToMillis(this.updatedAt)
        )
    }
    
    private fun MedicationEntity.toRequest(): MedicationRequest {
        return MedicationRequest(
            name = this.name,
            dosage = this.dosage,
            frequency = this.frequency,
            scheduleTimes = this.scheduleTimes.split(SCHEDULE_TIMES_SEPARATOR),
            startDate = this.startDate.toString(),
            endDate = this.endDate?.toString(),
            notes = this.notes,
            color = this.color,
            reminderEnabled = this.reminderEnabled,
            isActive = this.isActive,
            localId = this.id
        )
    }

    private fun buildMedicationRequest(medication: MedicationEntity): MedicationRequest {
        return medication.toRequest()
    }

    private fun mapServerMedicationToEntity(
        serverMedication: MedicationResponse,
        localId: String,
        serverId: String?
    ): MedicationEntity {
        return serverMedication.toEntity(localId = localId)
    }

    private fun findMatchingLocalMedication(
        allMedications: List<MedicationEntity>,
        serverMedication: MedicationResponse
    ): MedicationEntity? {
        return allMedications.find {
            it.name == serverMedication.name &&
            it.dosage == serverMedication.dosage &&
            it.serverId == null
        }
    }

    // ============================================================================
    // Private Helpers - Intake Creation
    // ============================================================================

    private suspend fun createIntakesForMedication(
        medication: MedicationEntity,
        scheduleTimes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate?
    ) {
        val finalEndDate = endDate ?: startDate.plusDays(DEFAULT_MEDICATION_DURATION_DAYS.toLong())
        var currentDate = startDate
        var intakeCount = 0

        Log.d("MedicationRepository", "Creating intakes from $startDate to $finalEndDate for medication ${medication.id}")

        while (currentDate <= finalEndDate) {
            val currentDayOfWeek = currentDate.dayOfWeek
            scheduleTimes.forEach { scheduleTimeString ->
                // Parse schedule time format: "MONDAY,THURSDAY:08:00" or legacy "08:00"
                val parsed = parseScheduleTime(scheduleTimeString)
                if (parsed != null) {
                    val (days, time) = parsed
                    // Only create intake if current day matches the scheduled days
                    if (days.contains(currentDayOfWeek)) {
                        createIntakeForTime(medication, currentDate, time)
                        intakeCount++
                    }
                } else {
                    // Legacy format: just time (e.g., "08:00") - create for all days
                    DateTimeUtils.parseTimeString(scheduleTimeString)?.let { time ->
                        createIntakeForTime(medication, currentDate, time)
                        intakeCount++
                    }
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        Log.d("MedicationRepository", "Created $intakeCount intakes for medication ${medication.id}")
    }

    private suspend fun createIntakeForTime(
        medication: MedicationEntity,
        date: LocalDate,
        time: LocalTime
    ) {
        try {
            val scheduledMillis = DateTimeUtils.dateAndTimeToMillis(date, time)
            val intake = MedicationIntakeEntity(
                id = UUID.randomUUID().toString(),
                medicationId = medication.id,
                scheduledTime = scheduledMillis,
                status = IntakeStatus.PENDING,
                syncStatus = SyncStatus.PENDING
            )
            intakeDao.insert(intake)

            if (medication.reminderEnabled) {
                notificationScheduler.scheduleIntakeReminder(intake.id, scheduledMillis)
            }
        } catch (e: Exception) {
            Log.w("MedicationRepository", "Failed to create intake for $date at $time: ${e.message}")
        }
    }

    private suspend fun createIntakeForTime(
        medication: MedicationEntity,
        date: LocalDate,
        timeString: String
    ) {
        try {
            DateTimeUtils.parseTimeString(timeString)?.let { time ->
                createIntakeForTime(medication, date, time)
            }
        } catch (e: Exception) {
            // Skip invalid time format
        }
    }

    // ============================================================================
    // Private Helpers - Intake Updates
    // ============================================================================

    private fun hasScheduleTimesChanged(
        existing: MedicationEntity,
        updated: MedicationEntity
    ): Boolean {
        val existingTimes = existing.scheduleTimes.split(SCHEDULE_TIMES_SEPARATOR).sorted()
        val updatedTimes = updated.scheduleTimes.split(SCHEDULE_TIMES_SEPARATOR).sorted()
        return existingTimes != updatedTimes ||
            existing.startDate != updated.startDate ||
            existing.endDate != updated.endDate
    }

    private suspend fun updateIntakesForScheduleChange(
        updatedMedication: MedicationEntity,
        existingMedication: MedicationEntity
    ) {
        val now = System.currentTimeMillis()
        val scheduleTimes = updatedMedication.scheduleTimes.split(SCHEDULE_TIMES_SEPARATOR)
        val startDate = updatedMedication.startDate
        val endDate = updatedMedication.endDate

        val existingIntakes = intakeDao.getIntakesForMedication(updatedMedication.id).first()
        val futureIntakes = existingIntakes.filter { 
            it.scheduledTime > now && it.status == IntakeStatus.PENDING 
        }

        // Delete future intakes (both locally and from server)
        futureIntakes.forEach { intake ->
            intakeDao.delete(intake)
            notificationScheduler.cancelIntakeReminder(intake.id)
            
            // Delete from server if it was synced
            intake.serverId?.let { serverId ->
                try {
                    // Note: Assuming there's a delete intake endpoint
                    Log.d("MedicationRepository", "Deleting intake ${intake.id} from server")
                } catch (e: Exception) {
                    Log.w("MedicationRepository", "Failed to delete intake from server: ${e.message}")
                }
            }
        }

        val today = LocalDate.now()
        val effectiveStartDate = if (startDate.isBefore(today)) today else startDate
        createIntakesForMedication(updatedMedication, scheduleTimes, effectiveStartDate, endDate)
        
        // Sync new intakes to server
        try {
            syncIntakesForMedication(updatedMedication.id)
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Failed to sync new intakes after schedule change", e)
        }
    }

    // ============================================================================
    // Private Helpers - Duplicate Removal
    // ============================================================================

    private fun groupMedicationsByUniqueKey(
        medications: List<MedicationEntity>
    ): Map<String, List<MedicationEntity>> {
        return medications.groupBy { medication ->
            medication.serverId ?: buildMedicationKey(medication.name, medication.dosage)
        }
    }

    private fun buildMedicationKey(name: String, dosage: String): String {
        return "${name}_$dosage"
    }

    private suspend fun removeDuplicateMedications(duplicates: List<MedicationEntity>) {
        val sortedByRecency = duplicates.sortedByDescending { it.updatedAt }
        val medicationToKeep = sortedByRecency.first()
        val medicationsToDelete = sortedByRecency.drop(1)

        medicationsToDelete.forEach { duplicate ->
            medicationDao.delete(duplicate)
        }
    }
}
