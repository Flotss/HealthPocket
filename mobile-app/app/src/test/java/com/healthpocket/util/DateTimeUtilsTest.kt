package com.healthpocket.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class DateTimeUtilsTest {

    @Test
    fun `parseTimeString with valid HH mm format returns LocalTime`() {
        val result = DateTimeUtils.parseTimeString("08:30")
        
        assertEquals(LocalTime.of(8, 30), result)
    }

    @Test
    fun `parseTimeString with valid HH mm format at midnight returns LocalTime`() {
        val result = DateTimeUtils.parseTimeString("00:00")
        
        assertEquals(LocalTime.of(0, 0), result)
    }

    @Test
    fun `parseTimeString with invalid format returns null`() {
        val result = DateTimeUtils.parseTimeString("invalid")
        
        assertNull(result)
    }

    @Test
    fun `parseTimeString with empty string returns null`() {
        val result = DateTimeUtils.parseTimeString("")
        
        assertNull(result)
    }

    @Test
    fun `parseTimeString with invalid hour returns null`() {
        val result = DateTimeUtils.parseTimeString("25:00")
        
        assertNull(result)
    }

    @Test
    fun `dateAndTimeToMillis converts LocalDate and LocalTime to milliseconds`() {
        val date = LocalDate.of(2024, 1, 15)
        val time = LocalTime.of(14, 30)
        
        val result = DateTimeUtils.dateAndTimeToMillis(date, time)
        assertTrue(result > 0)
    }

    @Test
    fun `millisToOffsetDateTimeString converts milliseconds to ISO string`() {
        val millis = 1705324800000L
        
        val result = DateTimeUtils.millisToOffsetDateTimeString(millis)
        
        assertNotNull(result)
        assertTrue(result.contains("2024-01-15"))
    }

    @Test
    fun `offsetDateTimeStringToMillis converts ISO string to milliseconds`() {
        val dateTimeString = "2024-01-15T14:00:00Z"
        
        val result = DateTimeUtils.offsetDateTimeStringToMillis(dateTimeString)
        assertTrue(result > 1705300000000L && result < 1705350000000L)
    }

    @Test
    fun `offsetDateTimeStringToMillis with invalid format returns zero`() {
        val result = DateTimeUtils.offsetDateTimeStringToMillis("invalid")
        
        assertEquals(0L, result)
    }

    @Test
    fun `parseDateString with valid ISO format returns LocalDate`() {
        val result = DateTimeUtils.parseDateString("2024-01-15")
        
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseDateStringOrNull with valid ISO format returns LocalDate`() {
        val result = DateTimeUtils.parseDateStringOrNull("2024-01-15")
        
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    @Test
    fun `parseDateStringOrNull with null returns null`() {
        val result = DateTimeUtils.parseDateStringOrNull(null)
        
        assertNull(result)
    }

    @Test
    fun `parseDateStringOrNull with invalid format returns null`() {
        val result = DateTimeUtils.parseDateStringOrNull("invalid-date")
        
        assertNull(result)
    }
}
