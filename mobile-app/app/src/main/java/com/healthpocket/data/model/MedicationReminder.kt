package com.healthpocket.data.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Represents a medication reminder with selected days and times.
 */
data class MedicationReminder(
    val days: Set<DayOfWeek> = emptySet(),
    val times: List<LocalTime> = emptyList()
) {
    fun isEmpty(): Boolean = days.isEmpty() || times.isEmpty()

    fun isValid(): Boolean = days.isNotEmpty() && times.isNotEmpty()
}
