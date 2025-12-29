package com.healthpocket.api.dto

import com.healthpocket.api.model.Appointment
import com.healthpocket.api.model.AppointmentStatus
import com.healthpocket.api.model.SyncStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Appointment request DTO.
 */
data class AppointmentRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,
    val doctorName: String? = null,
    val location: String? = null,

    @field:NotNull(message = "Appointment date is required")
    val appointmentDate: OffsetDateTime,

    @field:Positive(message = "Duration must be positive")
    val durationMinutes: Int = 30,

    val reminderMinutesBefore: Int = 60,
    val reminderEnabled: Boolean = true,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String? = null,
    val localId: String? = null
)

/**
 * Appointment response DTO.
 */
data class AppointmentResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val doctorName: String?,
    val location: String?,
    val appointmentDate: OffsetDateTime,
    val durationMinutes: Int,
    val reminderMinutesBefore: Int,
    val reminderEnabled: Boolean,
    val status: AppointmentStatus,
    val notes: String?,
    val syncStatus: SyncStatus,
    val localId: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(appointment: Appointment): AppointmentResponse {
            return AppointmentResponse(
                id = appointment.id!!,
                title = appointment.title,
                description = appointment.description,
                doctorName = appointment.doctorName,
                location = appointment.location,
                appointmentDate = appointment.appointmentDate,
                durationMinutes = appointment.durationMinutes,
                reminderMinutesBefore = appointment.reminderMinutesBefore,
                reminderEnabled = appointment.reminderEnabled,
                status = appointment.status,
                notes = appointment.notes,
                syncStatus = appointment.syncStatus,
                localId = appointment.localId,
                createdAt = appointment.createdAt,
                updatedAt = appointment.updatedAt
            )
        }
    }
}

/**
 * Update appointment status request DTO.
 */
data class UpdateAppointmentStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: AppointmentStatus
)

