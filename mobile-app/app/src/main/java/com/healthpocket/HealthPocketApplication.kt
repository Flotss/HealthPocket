package com.healthpocket

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for HealthPocket.
 * Initializes Hilt for dependency injection, WorkManager, and notification channels.
 */
@HiltAndroidApp
class HealthPocketApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Creates notification channels for medication reminders and appointment reminders.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Medication reminder channel
            val medicationChannel = NotificationChannel(
                MEDICATION_CHANNEL_ID,
                getString(R.string.notification_channel_medication),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_medication_desc)
                enableVibration(true)
                enableLights(true)
            }

            // Appointment reminder channel
            val appointmentChannel = NotificationChannel(
                APPOINTMENT_CHANNEL_ID,
                getString(R.string.notification_channel_appointment),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_appointment_desc)
                enableVibration(true)
                enableLights(true)
            }

            // Sync channel
            val syncChannel = NotificationChannel(
                SYNC_CHANNEL_ID,
                getString(R.string.notification_channel_sync),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_sync_desc)
            }

            notificationManager.createNotificationChannels(
                listOf(medicationChannel, appointmentChannel, syncChannel)
            )
        }
    }

    companion object {
        const val MEDICATION_CHANNEL_ID = "medication_reminders"
        const val APPOINTMENT_CHANNEL_ID = "appointment_reminders"
        const val SYNC_CHANNEL_ID = "sync_notifications"
    }
}

