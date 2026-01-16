package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Types of vital metrics that can be tracked.
 */
enum class MetricType {
    WEIGHT,
    BLOOD_PRESSURE,
    BLOOD_GLUCOSE,
    HEART_RATE,
    TEMPERATURE
}

/**
 * Room entity for vital signs/metrics.
 */
@Entity(tableName = "vital_metrics")
data class VitalMetricEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "server_id")
    val serverId: String? = null,

    @ColumnInfo(name = "metric_type")
    val metricType: MetricType,

    val value: Float,

    @ColumnInfo(name = "secondary_value")
    val secondaryValue: Float? = null, // For blood pressure (diastolic)

    val unit: String,

    @ColumnInfo(name = "measured_at")
    val measuredAt: Long,

    val notes: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

