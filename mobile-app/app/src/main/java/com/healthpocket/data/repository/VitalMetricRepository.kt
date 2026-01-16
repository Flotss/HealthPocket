package com.healthpocket.data.repository

import android.util.Log
import com.healthpocket.data.local.dao.VitalMetricDao
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.local.entity.VitalMetricEntity
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.VitalMetricRequest
import com.healthpocket.data.remote.dto.VitalMetricResponse
import com.healthpocket.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
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

    fun getRecentVitalMetricsByType(
        type: MetricType,
        limit: Int = 10
    ): Flow<List<VitalMetricEntity>> {
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
                Log.d("VitalMetricRepository", "Syncing deletion for metric ${metric.id}")
                api.deleteVitalMetric(serverId)
                Log.d("VitalMetricRepository", "Deletion synced for metric ${metric.id}")
            } catch (e: Exception) {
                Log.e("VitalMetricRepository", "Deletion sync failed for metric ${metric.id}", e)
            }
        }
    }

    private suspend fun trySync(metric: VitalMetricEntity) {
        Log.d("VitalMetricRepository", "Starting sync for metric ${metric.id}")
        try {
            val request = VitalMetricRequest(
                metricType = metric.metricType.name,
                value = metric.value,
                secondaryValue = metric.secondaryValue,
                unit = metric.unit,
                measuredAt = DateTimeUtils.millisToOffsetDateTimeString(metric.measuredAt),
                notes = metric.notes,
                localId = metric.id
            )

            val response = api.createVitalMetric(request)

            if (response.isSuccessful) {
                Log.d("VitalMetricRepository", "Sync successful for metric ${metric.id}")
                response.body()?.let { serverMetric ->
                    val synced = metric.copy(
                        serverId = serverMetric.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    vitalMetricDao.update(synced)
                }
            } else {
                Log.e(
                    "VitalMetricRepository",
                    "Sync failed for metric ${metric.id}: ${response.code()}"
                )
                vitalMetricDao.updateSyncStatus(metric.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.e("VitalMetricRepository", "Sync exception for metric ${metric.id}", e)
            vitalMetricDao.updateSyncStatus(metric.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingVitalMetrics(): List<VitalMetricEntity> {
        return vitalMetricDao.getVitalMetricsBySyncStatus(SyncStatus.PENDING)
    }

    suspend fun syncAll() {
        Log.d("VitalMetricRepository", "Starting bidirectional sync of vital metrics")
        try {
            val serverMetrics = api.getAllVitalMetrics().body() ?: emptyList()
            val localMetrics = vitalMetricDao.getAllVitalMetricsAsync()

            mergeAndSync(serverMetrics, localMetrics)

            Log.d("VitalMetricRepository", "Bidirectional sync completed successfully")
        } catch (e: Exception) {
            Log.e("VitalMetricRepository", "Bidirectional sync failed", e)
        }
    }

    private suspend fun mergeAndSync(
        serverMetrics: List<VitalMetricResponse>,
        localMetrics: List<VitalMetricEntity>
    ) {
        val localByServerId = localMetrics
            .filter { it.serverId != null }
            .associateBy { it.serverId!! }

        serverMetrics.forEach { serverMetric ->
            val localMatch = localByServerId[serverMetric.id]

            when {
                localMatch == null -> {
                    handleNewServerMetric(serverMetric)
                }

                isServerNewer(serverMetric.updatedAt, localMatch.updatedAt) -> {
                    handleServerNewerMetric(serverMetric, localMatch)
                }

                isLocalNewer(serverMetric.updatedAt, localMatch.updatedAt) -> {
                    handleLocalNewerMetric(serverMetric, localMatch)
                }
            }
        }

        val unsyncedLocalMetrics = localMetrics.filter { it.serverId == null }
        unsyncedLocalMetrics.forEach { localMetric ->
            pushLocalMetricToServer(localMetric)
        }
    }

    private suspend fun handleNewServerMetric(serverMetric: VitalMetricResponse) {
        Log.d("VitalMetricRepository", "Adding new metric from server: ${serverMetric.id}")
        val entity = serverMetric.toEntity()
        vitalMetricDao.insert(entity)
    }

    private suspend fun handleServerNewerMetric(
        serverMetric: VitalMetricResponse,
        localMetric: VitalMetricEntity
    ) {
        Log.d("VitalMetricRepository", "Updating local metric from server: ${serverMetric.id}")
        val updatedEntity = serverMetric.toEntity(localId = localMetric.id)
        vitalMetricDao.update(updatedEntity)
    }

    private suspend fun handleLocalNewerMetric(
        serverMetric: VitalMetricResponse,
        localMetric: VitalMetricEntity
    ) {
        Log.d("VitalMetricRepository", "Updating server with local metric: ${localMetric.id}")
        Log.w("VitalMetricRepository", "Cannot update server metric - API doesn't support updates")
    }

    private suspend fun pushLocalMetricToServer(localMetric: VitalMetricEntity) {
        Log.d("VitalMetricRepository", "Pushing local metric to server: ${localMetric.id}")
        try {
            val request = localMetric.toRequest()
            val response = api.createVitalMetric(request)

            if (response.isSuccessful) {
                response.body()?.let { serverMetric ->
                    vitalMetricDao.updateServerIdAndStatus(
                        localId = localMetric.id,
                        serverId = serverMetric.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                Log.e(
                    "VitalMetricRepository",
                    "Failed to create metric on server: ${response.code()}"
                )
                vitalMetricDao.updateSyncStatus(localMetric.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("VitalMetricRepository", "Network error pushing metric to server: ${e.message}")
        }
    }

    private fun isServerNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return serverMillis > localUpdatedAt
    }

    private fun isLocalNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return localUpdatedAt > serverMillis
    }

    private fun VitalMetricResponse.toEntity(
        localId: String = UUID.randomUUID().toString()
    ): VitalMetricEntity {
        return VitalMetricEntity(
            id = localId,
            serverId = this.id,
            metricType = MetricType.valueOf(this.metricType),
            value = this.value,
            secondaryValue = this.secondaryValue,
            unit = this.unit,
            measuredAt = DateTimeUtils.offsetDateTimeStringToMillis(this.measuredAt),
            notes = this.notes,
            syncStatus = SyncStatus.SYNCED,
            updatedAt = DateTimeUtils.offsetDateTimeStringToMillis(this.updatedAt)
        )
    }

    private fun VitalMetricEntity.toRequest(): VitalMetricRequest {
        return VitalMetricRequest(
            metricType = this.metricType.name,
            value = this.value,
            secondaryValue = this.secondaryValue,
            unit = this.unit,
            measuredAt = DateTimeUtils.millisToOffsetDateTimeString(this.measuredAt),
            notes = this.notes,
            localId = this.id
        )
    }
}

