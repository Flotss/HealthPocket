package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.VitalMetricDao
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.local.entity.VitalMetricEntity
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.VitalMetricRequest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for vital metrics data operations.
 */
@Singleton
class VitalMetricRepository @Inject constructor(
    private val vitalMetricDao: VitalMetricDao,
    private val api: HealthPocketApi
) {

    fun getAllVitalMetrics(): Flow<List<VitalMetricEntity>> {
        return vitalMetricDao.getAllVitalMetrics()
    }

    fun getVitalMetricsByType(type: MetricType): Flow<List<VitalMetricEntity>> {
        return vitalMetricDao.getVitalMetricsByType(type)
    }

    fun getRecentVitalMetricsByType(type: MetricType, limit: Int = 10): Flow<List<VitalMetricEntity>> {
        return vitalMetricDao.getRecentVitalMetricsByType(type, limit)
    }

    fun getLatestVitalMetricByType(type: MetricType): Flow<VitalMetricEntity?> {
        return vitalMetricDao.getLatestVitalMetricByType(type)
    }

    fun getVitalMetricsInRange(startTime: Long, endTime: Long): Flow<List<VitalMetricEntity>> {
        return vitalMetricDao.getVitalMetricsInRange(startTime, endTime)
    }

    suspend fun createVitalMetric(
        metricType: MetricType,
        value: Float,
        secondaryValue: Float? = null,
        unit: String,
        measuredAt: Long = System.currentTimeMillis(),
        notes: String? = null
    ): VitalMetricEntity {
        val metric = VitalMetricEntity(
            id = UUID.randomUUID().toString(),
            metricType = metricType,
            value = value,
            secondaryValue = secondaryValue,
            unit = unit,
            measuredAt = measuredAt,
            notes = notes,
            syncStatus = SyncStatus.PENDING
        )

        vitalMetricDao.insert(metric)
        trySync(metric)
        
        return metric
    }

    suspend fun deleteVitalMetric(metric: VitalMetricEntity) {
        vitalMetricDao.delete(metric)
        metric.serverId?.let { serverId ->
            try {
                api.deleteVitalMetric(serverId)
            } catch (e: Exception) {
                // Ignore network errors
            }
        }
    }

    private suspend fun trySync(metric: VitalMetricEntity) {
        try {
            val request = VitalMetricRequest(
                metricType = metric.metricType.name,
                value = metric.value,
                secondaryValue = metric.secondaryValue,
                unit = metric.unit,
                measuredAt = Instant.ofEpochMilli(metric.measuredAt)
                    .atOffset(ZoneOffset.UTC).toString(),
                notes = metric.notes,
                localId = metric.id
            )

            val response = api.createVitalMetric(request)

            if (response.isSuccessful) {
                response.body()?.let { serverMetric ->
                    val synced = metric.copy(
                        serverId = serverMetric.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    vitalMetricDao.update(synced)
                }
            }
        } catch (e: Exception) {
            vitalMetricDao.updateSyncStatus(metric.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingVitalMetrics(): List<VitalMetricEntity> {
        return vitalMetricDao.getVitalMetricsBySyncStatus(SyncStatus.PENDING)
    }

    /**
     * Get the appropriate unit for a metric type.
     */
    fun getUnitForType(type: MetricType): String {
        return when (type) {
            MetricType.WEIGHT -> "kg"
            MetricType.BLOOD_PRESSURE -> "mmHg"
            MetricType.BLOOD_GLUCOSE -> "mg/dL"
            MetricType.HEART_RATE -> "bpm"
            MetricType.TEMPERATURE -> "°C"
        }
    }
}

