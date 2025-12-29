package com.healthpocket.api.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Medication entity for tracking user medications.
 */
@Entity
@Table(name = "medications")
data class Medication(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var dosage: String,

    @Column(nullable = false)
    var frequency: String,

    @Column(name = "schedule_times", nullable = false, columnDefinition = "TEXT")
    var scheduleTimes: String, // JSON array ["08:00", "14:00", "20:00"]

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    var color: String = "#4CAF50",

    @Column(name = "reminder_enabled")
    var reminderEnabled: Boolean = true,

    @Column(name = "is_active")
    var isActive: Boolean = true,

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

