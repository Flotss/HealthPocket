package com.healthpocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MedicationIntake operations.
 */
@Dao
interface MedicationIntakeDao {

    @Query("SELECT * FROM medication_intakes ORDER BY scheduled_time DESC")
    suspend fun getAllIntakesAsync(): List<MedicationIntakeEntity>

    @Query("SELECT * FROM medication_intakes WHERE medication_id = :medicationId ORDER BY scheduled_time DESC")
    fun getIntakesForMedication(medicationId: String): Flow<List<MedicationIntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE scheduled_time BETWEEN :startTime AND :endTime ORDER BY scheduled_time ASC")
    fun getIntakesInRange(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE id = :id")
    suspend fun getIntakeById(id: String): MedicationIntakeEntity?

    @Query("SELECT * FROM medication_intakes WHERE server_id = :serverId")
    suspend fun getIntakeByServerId(serverId: String): MedicationIntakeEntity?

    @Query("SELECT * FROM medication_intakes WHERE status = :status")
    fun getIntakesByStatus(status: IntakeStatus): Flow<List<MedicationIntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE sync_status = :status")
    suspend fun getIntakesBySyncStatus(status: SyncStatus): List<MedicationIntakeEntity>

    @Query(
        """
        SELECT * FROM medication_intakes 
        WHERE scheduled_time BETWEEN :startTime AND :endTime 
        AND status = 'PENDING'
        ORDER BY scheduled_time ASC
    """
    )
    fun getTodaysPendingIntakes(startTime: Long, endTime: Long): Flow<List<MedicationIntakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intake: MedicationIntakeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(intakes: List<MedicationIntakeEntity>)

    @Update
    suspend fun update(intake: MedicationIntakeEntity)

    @Delete
    suspend fun delete(intake: MedicationIntakeEntity)

    @Query("UPDATE medication_intakes SET status = :status, taken_time = :takenTime, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateIntakeStatus(
        id: String,
        status: IntakeStatus,
        takenTime: Long?,
        syncStatus: SyncStatus
    )

    @Query("UPDATE medication_intakes SET server_id = :serverId, sync_status = :status WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: String, serverId: String, status: SyncStatus)
}

