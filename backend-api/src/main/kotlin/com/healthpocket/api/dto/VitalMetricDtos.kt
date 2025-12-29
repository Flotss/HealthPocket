package com.healthpocket.api.dto

import com.healthpocket.api.model.MetricType
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.model.VitalMetric
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Vital metric request DTO.
 */
data class VitalMetricRequest(
    @field:NotNull(message = "Metric type is required")
    val metricType: MetricType,

    @field:NotNull(message = "Value is required")
    val value: BigDecimal,

    val secondaryValue: BigDecimal? = null,

    @field:NotBlank(message = "Unit is required")
    val unit: String,

    @field:NotNull(message = "Measured at is required")
    val measuredAt: OffsetDateTime,

    val notes: String? = null,
    val localId: String? = null
)

/**
 * Vital metric response DTO.
 */
data class VitalMetricResponse(
    val id: UUID,
    val metricType: MetricType,
    val value: BigDecimal,
    val secondaryValue: BigDecimal?,
    val unit: String,
    val measuredAt: OffsetDateTime,
    val notes: String?,
    val syncStatus: SyncStatus,
    val localId: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(metric: VitalMetric): VitalMetricResponse {
            return VitalMetricResponse(
                id = metric.id!!,
                metricType = metric.metricType,
                value = metric.value,
                secondaryValue = metric.secondaryValue,
                unit = metric.unit,
                measuredAt = metric.measuredAt,
                notes = metric.notes,
                syncStatus = metric.syncStatus,
                localId = metric.localId,
                createdAt = metric.createdAt,
                updatedAt = metric.updatedAt
            )
        }
    }
}

/**
 * Vital metrics summary for dashboard.
 */
data class VitalMetricsSummary(
    val latestWeight: VitalMetricResponse?,
    val latestBloodPressure: VitalMetricResponse?,
    val latestBloodGlucose: VitalMetricResponse?,
    val latestHeartRate: VitalMetricResponse?,
    val latestTemperature: VitalMetricResponse?
)

