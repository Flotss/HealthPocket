package com.healthpocket.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Handles scheduling of notifications using AlarmManager.
 */
class NotificationScheduler(private val context: Context) {

    companion object {
        const val EXTRA_INTAKE_ID = "intake_id"
        const val EXTRA_INTAKE_TIME = "intake_time"
    }

    fun scheduleIntakeReminder(intakeId: String, scheduledTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Don't schedule if time is in the past
        if (scheduledTimeMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.healthpocket.INTAKE_REMINDER"
            putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, intakeId.hashCode())
            putExtra(
                NotificationReceiver.EXTRA_NOTIFICATION_TYPE,
                NotificationReceiver.TYPE_MEDICATION
            )
            putExtra(NotificationReceiver.EXTRA_TITLE, "Medicament à prendre")
            putExtra(
                NotificationReceiver.EXTRA_MESSAGE,
                "N'oubliez pas de prendre votre médicament"
            )
            putExtra(EXTRA_INTAKE_ID, intakeId)
            putExtra(EXTRA_INTAKE_TIME, scheduledTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                // Older versions
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if SCHEDULE_EXACT_ALARM permission not granted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelIntakeReminder(intakeId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}


