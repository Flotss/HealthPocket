package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.MedicationRequest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
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
    private val api: HealthPocketApi
) {

    // Medications
    
    fun getAllMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getAllMedications()
    }

    fun getActiveMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getActiveMedications()
    }

    fun getMedicationById(id: String): Flow<MedicationEntity?> {
        return medicationDao.getMedicationById(id)
    }

    suspend fun createMedication(
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        notes: String? = null,
        color: String = "#4CAF50",
        reminderEnabled: Boolean = true
    ): MedicationEntity {
        val medication = MedicationEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            dosage = dosage,
            frequency = frequency,
            scheduleTimes = scheduleTimes.joinToString(","),
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            color = color,
            reminderEnabled = reminderEnabled,
            isActive = true,
            syncStatus = SyncStatus.PENDING
        )

        medicationDao.insert(medication)
        
        // Try to sync to server
        trySync(medication)
        
        return medication
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        val updated = medication.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        medicationDao.update(updated)
        trySync(updated)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.delete(medication)
        // Also delete from server if synced
        medication.serverId?.let { serverId ->
            try {
                api.deleteMedication(serverId)
            } catch (e: Exception) {
                // Ignore network errors for deletion
            }
        }
    }

    private suspend fun trySync(medication: MedicationEntity) {
        try {
            val request = MedicationRequest(
                name = medication.name,
                dosage = medication.dosage,
                frequency = medication.frequency,
                scheduleTimes = medication.scheduleTimes.split(","),
                startDate = medication.startDate.toString(),
                endDate = medication.endDate?.toString(),
                notes = medication.notes,
                color = medication.color,
                reminderEnabled = medication.reminderEnabled,
                isActive = medication.isActive,
                localId = medication.id
            )

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
            }
        } catch (e: Exception) {
            // Network error - keep as pending
            medicationDao.updateSyncStatus(medication.id, SyncStatus.ERROR)
        }
    }

    // Medication Intakes

    fun getIntakesForMedication(medicationId: String): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getIntakesForMedication(medicationId)
    }

    fun getIntakesInRange(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getIntakesInRange(startTime, endTime)
    }

    fun getTodaysPendingIntakes(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>> {
        return intakeDao.getTodaysPendingIntakes(startTime, endTime)
    }

    suspend fun createIntake(
        medicationId: String,
        scheduledTime: Long
    ): MedicationIntakeEntity {
        val intake = MedicationIntakeEntity(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            scheduledTime = scheduledTime,
            status = IntakeStatus.PENDING,
            syncStatus = SyncStatus.PENDING
        )
        intakeDao.insert(intake)
        return intake
    }

    suspend fun markIntakeAsTaken(intake: MedicationIntakeEntity) {
        val now = System.currentTimeMillis()
        intakeDao.updateIntakeStatus(
            id = intake.id,
            status = IntakeStatus.TAKEN,
            takenTime = now,
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
    }

    suspend fun getPendingMedications(): List<MedicationEntity> {
        return medicationDao.getMedicationsBySyncStatus(SyncStatus.PENDING)
    }
}

