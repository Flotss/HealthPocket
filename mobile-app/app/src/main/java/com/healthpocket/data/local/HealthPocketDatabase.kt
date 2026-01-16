package com.healthpocket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.dao.HealthLogDao
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.dao.UserDao
import com.healthpocket.data.local.dao.VitalMetricDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.local.entity.UserEntity
import com.healthpocket.data.local.entity.VitalMetricEntity

/**
 * Main Room database for the HealthPocket application.
 */
@Database(
    entities = [
        UserEntity::class,
        MedicationEntity::class,
        MedicationIntakeEntity::class,
        AppointmentEntity::class,
        HealthLogEntity::class,
        VitalMetricEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HealthPocketDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationIntakeDao(): MedicationIntakeDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun healthLogDao(): HealthLogDao
    abstract fun vitalMetricDao(): VitalMetricDao

    companion object {
        const val DATABASE_NAME = "healthpocket_db"
    }
}

