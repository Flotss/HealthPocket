package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.Medication
import com.healthpocket.api.model.MedicationIntake
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.MedicationIntakeRepository
import com.healthpocket.api.repository.MedicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class MedicationService(
    private val medicationRepository: MedicationRepository,
    private val medicationIntakeRepository: MedicationIntakeRepository
) {

    fun getAllMedications(userId: UUID): List<MedicationResponse> {
        return medicationRepository.findByUserId(userId)
            .map { MedicationResponse.fromEntity(it) }
    }

    fun getActiveMedications(userId: UUID): List<MedicationResponse> {
        return medicationRepository.findByUserIdAndIsActive(userId, true)
            .map { MedicationResponse.fromEntity(it) }
    }

    fun getMedicationById(userId: UUID, medicationId: UUID): MedicationResponse {
        val medication = findMedicationByIdAndUser(userId, medicationId)
        return MedicationResponse.fromEntity(medication)
    }

    @Transactional
    fun createMedication(userId: UUID, request: MedicationRequest): MedicationResponse {
        // Check if medication with same localId already exists
        request.localId?.let { localId ->
            medicationRepository.findByLocalId(localId)?.let {
                return updateMedicationByLocalId(userId, localId, request)
            }
        }

        val medication = Medication(
            userId = userId,
            name = request.name,
            dosage = request.dosage,
            frequency = request.frequency,
            scheduleTimes = toJsonArray(request.scheduleTimes),
            startDate = request.startDate,
            endDate = request.endDate,
            notes = request.notes,
            color = request.color,
            reminderEnabled = request.reminderEnabled,
            isActive = request.isActive,
            syncStatus = SyncStatus.SYNCED,
            localId = request.localId
        )

        val savedMedication = medicationRepository.save(medication)
        return MedicationResponse.fromEntity(savedMedication)
    }

    @Transactional
    fun updateMedication(userId: UUID, medicationId: UUID, request: MedicationRequest): MedicationResponse {
        val medication = findMedicationByIdAndUser(userId, medicationId)

        medication.name = request.name
        medication.dosage = request.dosage
        medication.frequency = request.frequency
        medication.scheduleTimes = toJsonArray(request.scheduleTimes)
        medication.startDate = request.startDate
        medication.endDate = request.endDate
        medication.notes = request.notes
        medication.color = request.color
        medication.reminderEnabled = request.reminderEnabled
        medication.isActive = request.isActive
        medication.syncStatus = SyncStatus.SYNCED

        val savedMedication = medicationRepository.save(medication)
        return MedicationResponse.fromEntity(savedMedication)
    }

    private fun updateMedicationByLocalId(userId: UUID, localId: String, request: MedicationRequest): MedicationResponse {
        val medication = medicationRepository.findByLocalId(localId)
            ?: throw ResourceNotFoundException("Medication not found with localId: $localId")

        if (medication.userId != userId) {
            throw ResourceNotFoundException("Medication not found")
        }

        medication.name = request.name
        medication.dosage = request.dosage
        medication.frequency = request.frequency
        medication.scheduleTimes = toJsonArray(request.scheduleTimes)
        medication.startDate = request.startDate
        medication.endDate = request.endDate
        medication.notes = request.notes
        medication.color = request.color
        medication.reminderEnabled = request.reminderEnabled
        medication.isActive = request.isActive
        medication.syncStatus = SyncStatus.SYNCED

        val savedMedication = medicationRepository.save(medication)
        return MedicationResponse.fromEntity(savedMedication)
    }

    @Transactional
    fun deleteMedication(userId: UUID, medicationId: UUID) {
        val medication = findMedicationByIdAndUser(userId, medicationId)
        medicationRepository.delete(medication)
    }

    // Medication Intakes

    fun getIntakesForMedication(userId: UUID, medicationId: UUID): List<MedicationIntakeResponse> {
        findMedicationByIdAndUser(userId, medicationId) // Verify ownership
        return medicationIntakeRepository.findByMedicationId(medicationId)
            .map { MedicationIntakeResponse.fromEntity(it) }
    }

    fun getIntakesInRange(userId: UUID, startDate: OffsetDateTime, endDate: OffsetDateTime): List<MedicationIntakeResponse> {
        return medicationIntakeRepository.findByUserIdAndScheduledTimeBetween(userId, startDate, endDate)
            .map { MedicationIntakeResponse.fromEntity(it) }
    }

    @Transactional
    fun createIntake(userId: UUID, request: MedicationIntakeRequest): MedicationIntakeResponse {
        findMedicationByIdAndUser(userId, request.medicationId) // Verify ownership

        request.localId?.let { localId ->
            medicationIntakeRepository.findByLocalId(localId)?.let { existing ->
                return updateIntakeByLocalId(userId, localId, request)
            }
        }

        val intake = MedicationIntake(
            medicationId = request.medicationId,
            userId = userId,
            scheduledTime = request.scheduledTime,
            takenTime = request.takenTime,
            status = request.status,
            notes = request.notes,
            syncStatus = SyncStatus.SYNCED,
            localId = request.localId
        )

        val savedIntake = medicationIntakeRepository.save(intake)
        return MedicationIntakeResponse.fromEntity(savedIntake)
    }

    @Transactional
    fun updateIntakeStatus(userId: UUID, intakeId: UUID, request: UpdateIntakeStatusRequest): MedicationIntakeResponse {
        val intake = medicationIntakeRepository.findById(intakeId)
            .orElseThrow { ResourceNotFoundException("Intake not found with id: $intakeId") }

        if (intake.userId != userId) {
            throw ResourceNotFoundException("Intake not found")
        }

        intake.status = request.status
        request.takenTime?.let { intake.takenTime = it }
        request.notes?.let { intake.notes = it }
        intake.syncStatus = SyncStatus.SYNCED

        val savedIntake = medicationIntakeRepository.save(intake)
        return MedicationIntakeResponse.fromEntity(savedIntake)
    }

    private fun updateIntakeByLocalId(userId: UUID, localId: String, request: MedicationIntakeRequest): MedicationIntakeResponse {
        val intake = medicationIntakeRepository.findByLocalId(localId)
            ?: throw ResourceNotFoundException("Intake not found with localId: $localId")

        if (intake.userId != userId) {
            throw ResourceNotFoundException("Intake not found")
        }

        intake.scheduledTime = request.scheduledTime
        intake.takenTime = request.takenTime
        intake.status = request.status
        intake.notes = request.notes
        intake.syncStatus = SyncStatus.SYNCED

        val savedIntake = medicationIntakeRepository.save(intake)
        return MedicationIntakeResponse.fromEntity(savedIntake)
    }

    private fun findMedicationByIdAndUser(userId: UUID, medicationId: UUID): Medication {
        val medication = medicationRepository.findById(medicationId)
            .orElseThrow { ResourceNotFoundException("Medication not found with id: $medicationId") }

        if (medication.userId != userId) {
            throw ResourceNotFoundException("Medication not found")
        }

        return medication
    }

    private fun toJsonArray(items: List<String>): String {
        return items.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }
}

