package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.HealthLogDao
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.HealthLogRequest
import kotlinx.coroutines.flow.Flow
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
        return healthLogDao.getAllHealthLogs()
    }

    fun getRecentHealthLogs(limit: Int = 7): Flow<List<HealthLogEntity>> {
        return healthLogDao.getRecentHealthLogs(limit)
    }

    fun getHealthLogByDate(date: LocalDate): Flow<HealthLogEntity?> {
        return healthLogDao.getHealthLogByDate(date)
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
        // Check if log for this date already exists
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
                api.deleteHealthLog(serverId)
            } catch (e: Exception) {
                // Ignore network errors
            }
        }
    }

    private suspend fun trySync(healthLog: HealthLogEntity) {
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
                response.body()?.let { serverLog ->
                    val synced = healthLog.copy(
                        serverId = serverLog.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    healthLogDao.update(synced)
                }
            }
        } catch (e: Exception) {
            healthLogDao.updateSyncStatus(healthLog.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingHealthLogs(): List<HealthLogEntity> {
        return healthLogDao.getHealthLogsBySyncStatus(SyncStatus.PENDING)
    }
}

