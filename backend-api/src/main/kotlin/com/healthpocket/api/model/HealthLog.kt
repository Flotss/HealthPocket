package com.healthpocket.api.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * HealthLog entity for daily health journal entries.
 */
@Entity
@Table(
    name = "health_logs",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "log_date"])]
)
data class HealthLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "log_date", nullable = false)
    var logDate: LocalDate,

    var mood: Int? = null, // 1-5 scale

    @Column(name = "energy_level")
    var energyLevel: Int? = null, // 1-5 scale

    @Column(name = "sleep_quality")
    var sleepQuality: Int? = null, // 1-5 scale

    @Column(name = "sleep_hours", precision = 3, scale = 1)
    var sleepHours: BigDecimal? = null,

    @Column(columnDefinition = "TEXT")
    var symptoms: String? = null, // JSON array

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

