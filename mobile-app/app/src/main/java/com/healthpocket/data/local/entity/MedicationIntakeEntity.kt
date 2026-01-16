package com.healthpocket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Status of a medication intake.
 */
enum class IntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED
}

/**
 * Room entity for tracking medication intake history.
 */
@Entity(
    tableName = "medication_intakes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medication_id")]
)
data class MedicationIntakeEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "server_id")
    val serverId: String? = null,

    @ColumnInfo(name = "medication_id")
    val medicationId: String,

    @ColumnInfo(name = "scheduled_time")
    val scheduledTime: Long,

    @ColumnInfo(name = "taken_time")
    val takenTime: Long? = null,

    val status: IntakeStatus = IntakeStatus.PENDING,

    val notes: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

