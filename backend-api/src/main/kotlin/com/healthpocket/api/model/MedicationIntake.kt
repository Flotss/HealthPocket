package com.healthpocket.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

/**
 * MedicationIntake entity for tracking medication intake history.
 */
@Entity
@Table(name = "medication_intakes")
data class MedicationIntake(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "medication_id", nullable = false)
    val medicationId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "scheduled_time", nullable = false)
    var scheduledTime: OffsetDateTime,

    @Column(name = "taken_time")
    var takenTime: OffsetDateTime? = null,

    @Enumerated(EnumType.STRING)
    var status: IntakeStatus = IntakeStatus.PENDING,

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

enum class IntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED
}

