package com.healthpocket.data.local.dao

import androidx.room.*
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for HealthLog operations.
 */
@Dao
interface HealthLogDao {
    
    @Query("SELECT * FROM health_logs ORDER BY log_date DESC")
    fun getAllHealthLogs(): Flow<List<HealthLogEntity>>
    
    @Query("SELECT * FROM health_logs ORDER BY log_date DESC LIMIT :limit")
    fun getRecentHealthLogs(limit: Int): Flow<List<HealthLogEntity>>
    
    @Query("SELECT * FROM health_logs WHERE log_date = :date")
    fun getHealthLogByDate(date: LocalDate): Flow<HealthLogEntity?>
    
    @Query("SELECT * FROM health_logs WHERE log_date = :date")
    suspend fun getHealthLogByDateSync(date: LocalDate): HealthLogEntity?
    
    @Query("SELECT * FROM health_logs WHERE id = :id")
    suspend fun getHealthLogById(id: String): HealthLogEntity?
    
    @Query("SELECT * FROM health_logs WHERE sync_status = :status")
    suspend fun getHealthLogsBySyncStatus(status: SyncStatus): List<HealthLogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(healthLog: HealthLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(healthLogs: List<HealthLogEntity>)
    
    @Update
    suspend fun update(healthLog: HealthLogEntity)
    
    @Delete
    suspend fun delete(healthLog: HealthLogEntity)
    
    @Query("DELETE FROM health_logs WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("UPDATE health_logs SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
}

