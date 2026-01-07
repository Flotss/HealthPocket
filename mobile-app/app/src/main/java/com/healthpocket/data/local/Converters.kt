package com.healthpocket.data.local

import androidx.room.TypeConverter
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.local.entity.SyncStatus
import java.time.LocalDate

/**
 * Type converters for Room database.
 */
class Converters {
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
    
    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }
    
    // SyncStatus converters
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
    
    // IntakeStatus converters
    @TypeConverter
    fun fromIntakeStatus(status: IntakeStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toIntakeStatus(value: String): IntakeStatus {
        return IntakeStatus.valueOf(value)
    }
    
    // AppointmentStatus converters
    @TypeConverter
    fun fromAppointmentStatus(status: AppointmentStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toAppointmentStatus(value: String): AppointmentStatus {
        return AppointmentStatus.valueOf(value)
    }
    
    // MetricType converters
    @TypeConverter
    fun fromMetricType(type: MetricType): String {
        return type.name
    }
    
    @TypeConverter
    fun toMetricType(value: String): MetricType {
        return MetricType.valueOf(value)
    }
}

