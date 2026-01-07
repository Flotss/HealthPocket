package com.healthpocket.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.healthpocket.api.model.IntakeStatus
import com.healthpocket.api.model.Medication
import com.healthpocket.api.model.MedicationIntake
import com.healthpocket.api.model.SyncStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Medication request DTO.
 */
data class MedicationRequest(
    @field:NotBlank(message = "Medication name is required")
    val name: String,

    @field:NotBlank(message = "Dosage is required")
    val dosage: String,

    @field:NotBlank(message = "Frequency is required")
    val frequency: String,

    @field:NotEmpty(message = "Schedule times are required")
    val scheduleTimes: List<String>,

    @field:NotNull(message = "Start date is required")
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,

    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate? = null,
    val notes: String? = null,
    val color: String = "#4CAF50",
    val reminderEnabled: Boolean = true,
    val isActive: Boolean = true,
    val localId: String? = null
)

/**
 * Medication response DTO.
 */
data class MedicationResponse(
    val id: UUID,
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduleTimes: List<String>,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val startDate: LocalDate,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val endDate: LocalDate?,
    val notes: String?,
    val color: String,
    val reminderEnabled: Boolean,
    val isActive: Boolean,
    val syncStatus: SyncStatus,
    val localId: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(medication: Medication): MedicationResponse {
            return MedicationResponse(
                id = medication.id!!,
                name = medication.name,
                dosage = medication.dosage,
                frequency = medication.frequency,
                scheduleTimes = parseScheduleTimes(medication.scheduleTimes),
                startDate = medication.startDate,
                endDate = medication.endDate,
                notes = medication.notes,
                color = medication.color,
                reminderEnabled = medication.reminderEnabled,
                isActive = medication.isActive,
                syncStatus = medication.syncStatus,
                localId = medication.localId,
                createdAt = medication.createdAt,
                updatedAt = medication.updatedAt
            )
        }

        private fun parseScheduleTimes(json: String): List<String> {
            return try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * Medication intake request DTO.
 */
data class MedicationIntakeRequest(
    @field:NotNull(message = "Medication ID is required")
    val medicationId: UUID,

    @field:NotNull(message = "Scheduled time is required")
    val scheduledTime: OffsetDateTime,

    val takenTime: OffsetDateTime? = null,
    val status: IntakeStatus = IntakeStatus.PENDING,
    val notes: String? = null,
    val localId: String? = null
)

/**
 * Medication intake response DTO.
 */
data class MedicationIntakeResponse(
    val id: UUID,
    val medicationId: UUID,
    val scheduledTime: OffsetDateTime,
    val takenTime: OffsetDateTime?,
    val status: IntakeStatus,
    val notes: String?,
    val syncStatus: SyncStatus,
    val localId: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(intake: MedicationIntake): MedicationIntakeResponse {
            return MedicationIntakeResponse(
                id = intake.id!!,
                medicationId = intake.medicationId,
                scheduledTime = intake.scheduledTime,
                takenTime = intake.takenTime,
                status = intake.status,
                notes = intake.notes,
                syncStatus = intake.syncStatus,
                localId = intake.localId,
                createdAt = intake.createdAt,
                updatedAt = intake.updatedAt
            )
        }
    }
}

/**
 * Update intake status request DTO.
 */
data class UpdateIntakeStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: IntakeStatus,

    val takenTime: OffsetDateTime? = null,
    val notes: String? = null
)
