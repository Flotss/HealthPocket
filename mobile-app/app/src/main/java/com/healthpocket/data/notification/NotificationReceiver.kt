package com.healthpocket.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.healthpocket.HealthPocketApplication
import com.healthpocket.MainActivity
import com.healthpocket.R

/**
 * BroadcastReceiver for handling medication and appointment reminder notifications.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        
        const val TYPE_MEDICATION = "medication"
        const val TYPE_APPOINTMENT = "appointment"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: TYPE_MEDICATION
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""

        val channelId = when (type) {
            TYPE_MEDICATION -> HealthPocketApplication.MEDICATION_CHANNEL_ID
            TYPE_APPOINTMENT -> HealthPocketApplication.APPOINTMENT_CHANNEL_ID
            else -> HealthPocketApplication.MEDICATION_CHANNEL_ID
        }

        // Create intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            //.setSmallIcon(R.drawable.ic_notification) TODO: Add appropriate icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, notification)
            }
        }
    }
}

