package com.healthpocket.util

import com.healthpocket.data.model.MedicationReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleTimeUtilsTest {

    @Test
    fun `parseScheduleTime with valid format returns days and time`() {
        val result = parseScheduleTime("MONDAY,THURSDAY:08:00")
        
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), result?.first)
        assertEquals(LocalTime.of(8, 0), result?.second)
    }

    @Test
    fun `parseScheduleTime with single day returns correct result`() {
        val result = parseScheduleTime("MONDAY:14:30")
        
        assertEquals(setOf(DayOfWeek.MONDAY), result?.first)
        assertEquals(LocalTime.of(14, 30), result?.second)
    }

    @Test
    fun `parseScheduleTime with all days returns correct result`() {
        val result = parseScheduleTime("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY:12:00")
        
        assertEquals(DayOfWeek.values().toSet(), result?.first)
        assertEquals(LocalTime.of(12, 0), result?.second)
    }

    @Test
    fun `parseScheduleTime with invalid format returns null`() {
        val result = parseScheduleTime("invalid")
        
        assertNull(result)
    }

    @Test
    fun `parseScheduleTime with empty days returns null`() {
        val result = parseScheduleTime(":08:00")
        
        assertNull(result)
    }

    @Test
    fun `parseScheduleTime with invalid time returns null`() {
        val result = parseScheduleTime("MONDAY:25:00")
        
        assertNull(result)
    }

    @Test
    fun `remindersToScheduleTimes converts reminders to schedule times`() {
        val reminders = listOf(
            MedicationReminder(
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                times = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
            )
        )
        
        val result = remindersToScheduleTimes(reminders)
        
        assertEquals(2, result.size)
        assertEquals("MONDAY,WEDNESDAY:08:00", result[0])
        assertEquals("MONDAY,WEDNESDAY:20:00", result[1])
    }

    @Test
    fun `remindersToScheduleTimes filters invalid reminders`() {
        val reminders = listOf(
            MedicationReminder(days = emptySet(), times = listOf(LocalTime.of(8, 0))),
            MedicationReminder(days = setOf(DayOfWeek.MONDAY), times = emptyList())
        )
        
        val result = remindersToScheduleTimes(reminders)
        
        assertEquals(0, result.size)
    }

    @Test
    fun `scheduleTimesToReminders converts schedule times to reminders`() {
        val scheduleTimes = listOf("MONDAY,WEDNESDAY:08:00", "MONDAY,WEDNESDAY:20:00")
        
        val result = scheduleTimesToReminders(scheduleTimes)
        
        assertEquals(1, result.size)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), result[0].days)
        assertEquals(2, result[0].times.size)
        assertEquals(LocalTime.of(8, 0), result[0].times[0])
        assertEquals(LocalTime.of(20, 0), result[0].times[1])
    }

    @Test
    fun `scheduleTimesToReminders merges reminders with same days`() {
        val scheduleTimes = listOf("MONDAY:08:00", "MONDAY:20:00")
        
        val result = scheduleTimesToReminders(scheduleTimes)
        
        assertEquals(1, result.size)
        assertEquals(setOf(DayOfWeek.MONDAY), result[0].days)
        assertEquals(2, result[0].times.size)
    }

    @Test
    fun `scheduleTimesToReminders handles old format without days`() {
        val scheduleTimes = listOf("08:00", "20:00")
        
        val result = scheduleTimesToReminders(scheduleTimes)
        
        assertEquals(1, result.size)
        assertEquals(DayOfWeek.values().toSet(), result[0].days)
        assertEquals(2, result[0].times.size)
    }

    @Test
    fun `scheduleTimesToReminders ignores invalid formats`() {
        val scheduleTimes = listOf("invalid", "MONDAY:08:00")
        
        val result = scheduleTimesToReminders(scheduleTimes)
        
        assertEquals(1, result.size)
        assertEquals(setOf(DayOfWeek.MONDAY), result[0].days)
    }
}
