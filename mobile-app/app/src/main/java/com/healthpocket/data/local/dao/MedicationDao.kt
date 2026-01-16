package com.healthpocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Medication operations.
 */
@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    suspend fun getAllMedicationsAsync(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE is_active = 1 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: String): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationByIdSync(id: String): MedicationEntity?

    @Query("SELECT * FROM medications WHERE server_id = :serverId")
    suspend fun getMedicationByServerId(serverId: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<MedicationEntity>)

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE medications SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE medications SET server_id = :serverId, sync_status = :status WHERE id = :localId")
    suspend fun updateServerIdAndStatus(localId: String, serverId: String, status: SyncStatus)

    @Query("DELETE FROM medications")
    suspend fun deleteAll()
}

