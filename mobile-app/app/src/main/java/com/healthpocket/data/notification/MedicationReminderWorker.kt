package com.healthpocket.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.healthpocket.data.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Worker for scheduling medication reminder notifications.
 */
@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicationRepository: MedicationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Get all pending intakes for today
            val intakes = medicationRepository.getTodaysPendingIntakes(startOfDay, endOfDay).first()
            intakes.forEach { intake ->
                // Schedule notification for this intake
                scheduleIntakeNotification(intake.id, intake.scheduledTime)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun scheduleIntakeNotification(intakeId: String, scheduledTimeMillis: Long) {
        val notificationScheduler = NotificationScheduler(applicationContext)
        notificationScheduler.scheduleIntakeReminder(intakeId, scheduledTimeMillis)
    }
}


