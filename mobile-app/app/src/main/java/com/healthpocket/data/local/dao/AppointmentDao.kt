package com.healthpocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Appointment operations.
 */
@Dao
interface AppointmentDao {
    
    @Query("SELECT * FROM appointments ORDER BY appointment_date ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>
    
    @Query("SELECT * FROM appointments WHERE appointment_date >= :fromDate AND status = 'SCHEDULED' ORDER BY appointment_date ASC")
    fun getUpcomingAppointments(fromDate: Long): Flow<List<AppointmentEntity>>
    
    @Query("SELECT * FROM appointments WHERE appointment_date BETWEEN :startDate AND :endDate ORDER BY appointment_date ASC")
    fun getAppointmentsInRange(startDate: Long, endDate: Long): Flow<List<AppointmentEntity>>
    
    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getAppointmentById(id: String): Flow<AppointmentEntity?>
    
    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentByIdSync(id: String): AppointmentEntity?
    
    @Query("SELECT * FROM appointments WHERE sync_status = :status")
    suspend fun getAppointmentsBySyncStatus(status: SyncStatus): List<AppointmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)
    
    @Update
    suspend fun update(appointment: AppointmentEntity)
    
    @Delete
    suspend fun delete(appointment: AppointmentEntity)
    
    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("UPDATE appointments SET status = :status, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateStatus(id: String, status: AppointmentStatus, syncStatus: SyncStatus)
    
    @Query("UPDATE appointments SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
}

