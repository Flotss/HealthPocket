package com.healthpocket.data.repository

import android.content.Context
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.notification.NotificationScheduler
import com.healthpocket.data.remote.api.HealthPocketApi
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
            createIntakesForMedication(medication, scheduleTimes, startDate, endDate)
        } catch (e: Exception) {
            // Log error but don't fail medication creation
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

        return intake
    }

    suspend fun markIntakeAsTaken(intake: MedicationIntakeEntity) {
        intakeDao.updateIntakeStatus(
            id = intake.id,
            status = IntakeStatus.TAKEN,
            takenTime = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
    }

    suspend fun markIntakeAsSkipped(intake: MedicationIntakeEntity) {
        intakeDao.updateIntakeStatus(
            id = intake.id,
            status = IntakeStatus.SKIPPED,
            takenTime = null,
            syncStatus = SyncStatus.PENDING
        )
        notificationScheduler.cancelIntakeReminder(intake.id)
    }

    // ============================================================================
    // Public API - Sync Operations
    // ============================================================================

    suspend fun syncMedications() {
        try {
            val serverMedications = api.getAllMedications().body() ?: return

            serverMedications.forEach { serverMedication ->
                syncServerMedicationToLocal(serverMedication)
            }
        } catch (e: Exception) {
            // Silent fail - offline-first pattern
        }
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

    private suspend fun syncMedicationToServer(medication: MedicationEntity) {
        try {
            val request = buildMedicationRequest(medication)
            val response = if (medication.serverId != null) {
                api.updateMedication(medication.serverId, request)
            } else {
                api.createMedication(request)
            }

            if (response.isSuccessful) {
                response.body()?.let { serverMedication ->
                    medicationDao.updateServerIdAndStatus(
                        localId = medication.id,
                        serverId = serverMedication.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                medicationDao.updateSyncStatus(medication.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.e("MedicationRepository", "Sync failed for medication ${medication.id}", e)
            medicationDao.updateSyncStatus(medication.id, SyncStatus.ERROR)
        }
    }

    private suspend fun deleteMedicationFromServer(serverId: String) {
        try {
            api.deleteMedication(serverId)
        } catch (e: Exception) {
            // Ignore network errors for deletion - offline-first pattern
        }
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

    private fun buildMedicationRequest(medication: MedicationEntity): MedicationRequest {
        return MedicationRequest(
            name = medication.name,
            dosage = medication.dosage,
            frequency = medication.frequency,
            scheduleTimes = medication.scheduleTimes.split(SCHEDULE_TIMES_SEPARATOR),
            startDate = medication.startDate.toString(),
            endDate = medication.endDate?.toString(),
            notes = medication.notes,
            color = medication.color,
            reminderEnabled = medication.reminderEnabled,
            isActive = medication.isActive,
            localId = medication.id
        )
    }

    private fun mapServerMedicationToEntity(
        serverMedication: MedicationResponse,
        localId: String,
        serverId: String?
    ): MedicationEntity {
        return MedicationEntity(
            id = localId,
            serverId = serverId,
            name = serverMedication.name,
            dosage = serverMedication.dosage,
            frequency = serverMedication.frequency,
            scheduleTimes = serverMedication.scheduleTimes.joinToString(SCHEDULE_TIMES_SEPARATOR),
            startDate = LocalDate.parse(serverMedication.startDate),
            endDate = serverMedication.endDate?.let { LocalDate.parse(it) },
            notes = serverMedication.notes,
            color = serverMedication.color ?: AppColors.MEDICATION_DEFAULT,
            reminderEnabled = serverMedication.reminderEnabled ?: true,
            isActive = serverMedication.isActive ?: true,
            syncStatus = SyncStatus.SYNCED,
            updatedAt = System.currentTimeMillis()
        )
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
                    }
                } else {
                    // Legacy format: just time (e.g., "08:00") - create for all days
                    DateTimeUtils.parseTimeString(scheduleTimeString)?.let { time ->
                        createIntakeForTime(medication, currentDate, time)
                    }
                }
            }
            currentDate = currentDate.plusDays(1)
        }
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
            // Skip invalid time format
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

        futureIntakes.forEach { intake ->
            intakeDao.delete(intake)
            notificationScheduler.cancelIntakeReminder(intake.id)
        }

        val today = LocalDate.now()
        val effectiveStartDate = if (startDate.isBefore(today)) today else startDate
        createIntakesForMedication(updatedMedication, scheduleTimes, effectiveStartDate, endDate)
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
