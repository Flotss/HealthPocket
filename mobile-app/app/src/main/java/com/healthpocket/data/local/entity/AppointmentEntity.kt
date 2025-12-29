package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Appointment status.
 */
enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}

/**
 * Room entity for medical appointments.
 */
@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "server_id")
    val serverId: String? = null,
    
    val title: String,
    
    val description: String? = null,
    
    @ColumnInfo(name = "doctor_name")
    val doctorName: String? = null,
    
    val location: String? = null,
    
    @ColumnInfo(name = "appointment_date")
    val appointmentDate: Long,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 30,
    
    @ColumnInfo(name = "reminder_minutes_before")
    val reminderMinutesBefore: Int = 60,
    
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,
    
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    
    val notes: String? = null,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

