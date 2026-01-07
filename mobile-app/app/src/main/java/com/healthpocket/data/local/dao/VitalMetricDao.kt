package com.healthpocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.local.entity.VitalMetricEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VitalMetric operations.
 */
@Dao
interface VitalMetricDao {
    
    @Query("SELECT * FROM vital_metrics ORDER BY measured_at DESC")
    fun getAllVitalMetrics(): Flow<List<VitalMetricEntity>>
    
    @Query("SELECT * FROM vital_metrics WHERE metric_type = :type ORDER BY measured_at DESC")
    fun getVitalMetricsByType(type: MetricType): Flow<List<VitalMetricEntity>>
    
    @Query("SELECT * FROM vital_metrics WHERE metric_type = :type ORDER BY measured_at DESC LIMIT :limit")
    fun getRecentVitalMetricsByType(type: MetricType, limit: Int): Flow<List<VitalMetricEntity>>
    
    @Query("SELECT * FROM vital_metrics WHERE metric_type = :type ORDER BY measured_at DESC LIMIT 1")
    fun getLatestVitalMetricByType(type: MetricType): Flow<VitalMetricEntity?>
    
    @Query("SELECT * FROM vital_metrics WHERE measured_at BETWEEN :startTime AND :endTime ORDER BY measured_at DESC")
    fun getVitalMetricsInRange(startTime: Long, endTime: Long): Flow<List<VitalMetricEntity>>
    
    @Query("SELECT * FROM vital_metrics WHERE id = :id")
    suspend fun getVitalMetricById(id: String): VitalMetricEntity?
    
    @Query("SELECT * FROM vital_metrics WHERE sync_status = :status")
    suspend fun getVitalMetricsBySyncStatus(status: SyncStatus): List<VitalMetricEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vitalMetric: VitalMetricEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vitalMetrics: List<VitalMetricEntity>)
    
    @Update
    suspend fun update(vitalMetric: VitalMetricEntity)
    
    @Delete
    suspend fun delete(vitalMetric: VitalMetricEntity)
    
    @Query("DELETE FROM vital_metrics WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("UPDATE vital_metrics SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
}

