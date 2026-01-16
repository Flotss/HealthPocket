package com.healthpocket.util

import com.healthpocket.data.model.MedicationReminder
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Utility functions for converting between MedicationReminder and scheduleTimes format.
 * Format: "MONDAY,THURSDAY:08:00" (days separated by comma, then colon, then time)
 */

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Converts a list of MedicationReminder to scheduleTimes format.
 * Format: "MONDAY,THURSDAY:08:00"
 * Only includes reminders that are valid (have both days and times).
 */
fun remindersToScheduleTimes(reminders: List<MedicationReminder>): List<String> {
    return reminders
        .filter { it.isValid() } // Only process valid reminders
        .flatMap { reminder ->
            reminder.times.map { time ->
                val daysString = reminder.days.joinToString(",") { it.name }
                "$daysString:${time.format(TIME_FORMATTER)}"
            }
        }
}

/**
 * Parses a scheduleTime string to extract days and time.
 * Format: "MONDAY,THURSDAY:08:00"
 * Returns null if format is invalid.
 */
fun parseScheduleTime(scheduleTime: String): Pair<Set<DayOfWeek>, LocalTime>? {
    return try {
        // Split at first colon to separate days from time
        val parts = scheduleTime.split(":", limit = 2)
        if (parts.size != 2) return null

        val daysString = parts[0]
        val timeString = parts[1]

        val days = daysString.split(",").mapNotNull { dayName ->
            try {
                DayOfWeek.valueOf(dayName.trim())
            } catch (e: Exception) {
                null
            }
        }.toSet()

        // Handle HH:mm or HH:mm:ss
        val timeParts = timeString.split(":")
        if (timeParts.size < 2) return null

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        val time = LocalTime.of(hour, minute)

        if (days.isEmpty()) return null

        Pair(days, time)
    } catch (e: Exception) {
        null
    }
}

/**
 * Converts scheduleTimes (old format without days) to MedicationReminder list.
 * For backward compatibility, if no days are specified, assumes all days.
 */
fun scheduleTimesToReminders(scheduleTimes: List<String>): List<MedicationReminder> {
    val reminders = mutableListOf<MedicationReminder>()

    scheduleTimes.forEach { scheduleTime ->
        val parsed = parseScheduleTime(scheduleTime)
        if (parsed != null) {
            val (days, time) = parsed
            // Try to merge with existing reminder that has same days
            val existingIndex = reminders.indexOfFirst { it.days == days }
            if (existingIndex >= 0) {
                val existing = reminders[existingIndex]
                if (!existing.times.contains(time)) {
                    reminders[existingIndex] = existing.copy(times = existing.times + time)
                }
            } else {
                reminders.add(MedicationReminder(days = days, times = listOf(time)))
            }
        } else {
            // Old format: just time (e.g., "08:00") - assume all days
            try {
                val timeParts = scheduleTime.split(":")
                if (timeParts.size >= 2) {
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    val time = LocalTime.of(hour, minute)
                    val existingIndex =
                        reminders.indexOfFirst { it.days == DayOfWeek.values().toSet() }
                    if (existingIndex >= 0) {
                        val existing = reminders[existingIndex]
                        if (!existing.times.contains(time)) {
                            reminders[existingIndex] = existing.copy(times = existing.times + time)
                        }
                    } else {
                        reminders.add(
                            MedicationReminder(
                                days = DayOfWeek.values().toSet(),
                                times = listOf(time)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Skip invalid format
            }
        }
    }

    return reminders
}
