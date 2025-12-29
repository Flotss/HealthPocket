package com.healthpocket.api.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * VitalMetric entity for tracking health vital signs.
 */
@Entity
@Table(name = "vital_metrics")
data class VitalMetric(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    var metricType: MetricType,

    @Column(nullable = false, precision = 10, scale = 2)
    var value: BigDecimal,

    @Column(name = "secondary_value", precision = 10, scale = 2)
    var secondaryValue: BigDecimal? = null, // For blood pressure diastolic

    @Column(nullable = false)
    var unit: String,

    @Column(name = "measured_at", nullable = false)
    var measuredAt: OffsetDateTime,

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

enum class MetricType {
    WEIGHT,
    BLOOD_PRESSURE,
    BLOOD_GLUCOSE,
    HEART_RATE,
    TEMPERATURE
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    ERROR
}

