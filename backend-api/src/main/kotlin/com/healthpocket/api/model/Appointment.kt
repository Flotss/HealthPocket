package com.healthpocket.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Appointment entity for tracking medical appointments.
 */
@Entity
@Table(name = "appointments")
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "doctor_name")
    var doctorName: String? = null,

    var location: String? = null,

    @Column(name = "appointment_date", nullable = false)
    var appointmentDate: OffsetDateTime,

    @Column(name = "duration_minutes")
    var durationMinutes: Int = 30,

    @Column(name = "reminder_minutes_before")
    var reminderMinutesBefore: Int = 60,

    @Column(name = "reminder_enabled")
    var reminderEnabled: Boolean = true,

    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus = AppointmentStatus.SCHEDULED,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    var syncStatus: SyncStatus = SyncStatus.SYNCED,

    @Column(name = "local_id")
    var localId: String? = null,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}

