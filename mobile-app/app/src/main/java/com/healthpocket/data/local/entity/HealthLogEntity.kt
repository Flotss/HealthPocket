package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity for daily health journal entries.
 */
@Entity(tableName = "health_logs")
data class HealthLogEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "server_id")
    val serverId: String? = null,

    @ColumnInfo(name = "log_date")
    val logDate: LocalDate,

    val mood: Int? = null, // 1-5 scale

    @ColumnInfo(name = "energy_level")
    val energyLevel: Int? = null, // 1-5 scale

    @ColumnInfo(name = "sleep_quality")
    val sleepQuality: Int? = null, // 1-5 scale

    @ColumnInfo(name = "sleep_hours")
    val sleepHours: Float? = null,

    val symptoms: String? = null, // JSON array

    val notes: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

