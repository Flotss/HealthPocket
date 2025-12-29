package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Sync status for offline-first functionality.
 */
enum class SyncStatus {
    SYNCED,
    PENDING,
    ERROR
}

/**
 * Room entity for Medication.
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "server_id")
    val serverId: String? = null,
    
    val name: String,
    
    val dosage: String,
    
    val frequency: String,
    
    @ColumnInfo(name = "schedule_times")
    val scheduleTimes: String, // JSON array of times
    
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,
    
    val notes: String? = null,
    
    val color: String = "#4CAF50",
    
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

