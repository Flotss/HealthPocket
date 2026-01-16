package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.HealthLogDao
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.HealthLogRequest
import com.healthpocket.data.remote.dto.HealthLogResponse
import com.healthpocket.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for health log data operations.
 */
@Singleton
class HealthLogRepository @Inject constructor(
    private val healthLogDao: HealthLogDao,
    private val api: HealthPocketApi
) {

    fun getAllHealthLogs(): Flow<List<HealthLogEntity>> {
        return healthLogDao.getAllHealthLogs().distinctUntilChanged()
    }

    fun getRecentHealthLogs(limit: Int = 7): Flow<List<HealthLogEntity>> {
        return healthLogDao.getRecentHealthLogs(limit).distinctUntilChanged()
    }

    fun getHealthLogByDate(date: LocalDate): Flow<HealthLogEntity?> {
        return healthLogDao.getHealthLogByDate(date).distinctUntilChanged()
    }

    suspend fun createOrUpdateHealthLog(
        date: LocalDate,
        mood: Int? = null,
        energyLevel: Int? = null,
        sleepQuality: Int? = null,
        sleepHours: Float? = null,
        symptoms: List<String>? = null,
        notes: String? = null
    ): HealthLogEntity {
        val existing = healthLogDao.getHealthLogByDateSync(date)

        val healthLog = if (existing != null) {
            existing.copy(
                mood = mood ?: existing.mood,
                energyLevel = energyLevel ?: existing.energyLevel,
                sleepQuality = sleepQuality ?: existing.sleepQuality,
                sleepHours = sleepHours ?: existing.sleepHours,
                symptoms = symptoms?.joinToString(",") ?: existing.symptoms,
                notes = notes ?: existing.notes,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            HealthLogEntity(
                id = UUID.randomUUID().toString(),
                logDate = date,
                mood = mood,
                energyLevel = energyLevel,
                sleepQuality = sleepQuality,
                sleepHours = sleepHours,
                symptoms = symptoms?.joinToString(","),
                notes = notes,
                syncStatus = SyncStatus.PENDING
            )
        }

        healthLogDao.insert(healthLog)
        trySync(healthLog)

        return healthLog
    }

    suspend fun deleteHealthLog(healthLog: HealthLogEntity) {
        healthLogDao.delete(healthLog)
        healthLog.serverId?.let { serverId ->
            try {
                android.util.Log.d(
                    "HealthLogRepository",
                    "Syncing deletion for log ${healthLog.id}"
                )
                api.deleteHealthLog(serverId)
                android.util.Log.d("HealthLogRepository", "Deletion synced for log ${healthLog.id}")
            } catch (e: Exception) {
                android.util.Log.e(
                    "HealthLogRepository",
                    "Deletion sync failed for log ${healthLog.id}",
                    e
                )
            }
        }
    }

    private suspend fun trySync(healthLog: HealthLogEntity) {
        android.util.Log.d("HealthLogRepository", "Starting sync for log ${healthLog.id}")
        try {
            val request = HealthLogRequest(
                logDate = healthLog.logDate.toString(),
                mood = healthLog.mood,
                energyLevel = healthLog.energyLevel,
                sleepQuality = healthLog.sleepQuality,
                sleepHours = healthLog.sleepHours,
                symptoms = healthLog.symptoms?.split(",")?.filter { it.isNotBlank() },
                notes = healthLog.notes,
                localId = healthLog.id
            )

            val response = api.createOrUpdateHealthLog(request)

            if (response.isSuccessful) {
                android.util.Log.d("HealthLogRepository", "Sync successful for log ${healthLog.id}")
                response.body()?.let { serverLog ->
                    val synced = healthLog.copy(
                        serverId = serverLog.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    healthLogDao.update(synced)
                }
            } else {
                android.util.Log.e(
                    "HealthLogRepository",
                    "Sync failed for log ${healthLog.id}: ${response.code()}"
                )
                healthLogDao.updateSyncStatus(healthLog.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthLogRepository", "Sync exception for log ${healthLog.id}", e)
            healthLogDao.updateSyncStatus(healthLog.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingHealthLogs(): List<HealthLogEntity> {
        return healthLogDao.getHealthLogsBySyncStatus(SyncStatus.PENDING)
    }

    suspend fun syncAll() {
        android.util.Log.d("HealthLogRepository", "Starting bidirectional sync of health logs")
        try {
            val serverLogs = api.getAllHealthLogs().body() ?: emptyList()
            val localLogs = healthLogDao.getAllHealthLogsAsync()

            mergeAndSync(serverLogs, localLogs)

            android.util.Log.d("HealthLogRepository", "Bidirectional sync completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("HealthLogRepository", "Bidirectional sync failed", e)
        }
    }

    @Deprecated("Use syncAll() for bidirectional sync", ReplaceWith("syncAll()"))
    suspend fun syncHealthLogs() {
        syncAll()
    }

    private suspend fun mergeAndSync(
        serverLogs: List<HealthLogResponse>,
        localLogs: List<HealthLogEntity>
    ) {
        serverLogs.associateBy { it.id }
        val localByServerId = localLogs
            .filter { it.serverId != null }
            .associateBy { it.serverId!! }

        serverLogs.forEach { serverLog ->
            val localMatch = localByServerId[serverLog.id]

            when {
                localMatch == null -> {
                    handleNewServerLog(serverLog)
                }

                isServerNewer(serverLog.updatedAt, localMatch.updatedAt) -> {
                    handleServerNewerLog(serverLog, localMatch)
                }

                isLocalNewer(serverLog.updatedAt, localMatch.updatedAt) -> {
                    handleLocalNewerLog(serverLog, localMatch)
                }
            }
        }

        val unsyncedLocalLogs = localLogs.filter { it.serverId == null }
        unsyncedLocalLogs.forEach { localLog ->
            pushLocalLogToServer(localLog)
        }
    }

    private suspend fun handleNewServerLog(serverLog: HealthLogResponse) {
        android.util.Log.d("HealthLogRepository", "Adding new log from server: ${serverLog.id}")
        val entity = serverLog.toEntity()
        healthLogDao.insert(entity)
    }

    private suspend fun handleServerNewerLog(
        serverLog: HealthLogResponse,
        localLog: HealthLogEntity
    ) {
        android.util.Log.d("HealthLogRepository", "Updating local log from server: ${serverLog.id}")
        val updatedEntity = serverLog.toEntity(localId = localLog.id)
        healthLogDao.update(updatedEntity)
    }

    private suspend fun handleLocalNewerLog(
        serverLog: HealthLogResponse,
        localLog: HealthLogEntity
    ) {
        android.util.Log.d("HealthLogRepository", "Updating server with local log: ${localLog.id}")
        try {
            val request = localLog.toRequest()
            val response = api.createOrUpdateHealthLog(request)

            if (response.isSuccessful) {
                healthLogDao.updateSyncStatus(localLog.id, SyncStatus.SYNCED)
            } else {
                android.util.Log.e(
                    "HealthLogRepository",
                    "Failed to update server log: ${response.code()}"
                )
                healthLogDao.updateSyncStatus(localLog.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.w(
                "HealthLogRepository",
                "Network error updating log to server: ${e.message}"
            )
        }
    }

    private suspend fun pushLocalLogToServer(localLog: HealthLogEntity) {
        android.util.Log.d("HealthLogRepository", "Pushing local log to server: ${localLog.id}")
        try {
            val request = localLog.toRequest()
            val response = api.createOrUpdateHealthLog(request)

            if (response.isSuccessful) {
                response.body()?.let { serverLog ->
                    healthLogDao.updateServerIdAndStatus(
                        localId = localLog.id,
                        serverId = serverLog.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                android.util.Log.e(
                    "HealthLogRepository",
                    "Failed to create log on server: ${response.code()}"
                )
                healthLogDao.updateSyncStatus(localLog.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.w(
                "HealthLogRepository",
                "Network error pushing log to server: ${e.message}"
            )
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

    private fun HealthLogResponse.toEntity(
        localId: String = UUID.randomUUID().toString()
    ): HealthLogEntity {
        return HealthLogEntity(
            id = localId,
            serverId = this.id,
            logDate = DateTimeUtils.parseDateString(this.logDate),
            mood = this.mood,
            energyLevel = this.energyLevel,
            sleepQuality = this.sleepQuality,
            sleepHours = this.sleepHours,
            symptoms = this.symptoms.takeIf { it.isNotEmpty() }?.joinToString(","),
            notes = this.notes,
            syncStatus = SyncStatus.SYNCED,
            updatedAt = DateTimeUtils.offsetDateTimeStringToMillis(this.updatedAt)
        )
    }

    private fun HealthLogEntity.toRequest(): HealthLogRequest {
        return HealthLogRequest(
            logDate = this.logDate.toString(),
            mood = this.mood,
            energyLevel = this.energyLevel,
            sleepQuality = this.sleepQuality,
            sleepHours = this.sleepHours,
            symptoms = this.symptoms?.split(",")?.filter { it.isNotBlank() },
            notes = this.notes,
            localId = this.id
        )
    }
}

