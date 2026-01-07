package com.healthpocket.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Utility functions for date and time operations.
 */
object DateTimeUtils {

    private const val TIME_SEPARATOR = ":"

    /**
     * Parses a time string (HH:mm) to LocalTime.
     */
    fun parseTimeString(timeString: String): LocalTime? {
        return try {
            val parts = timeString.split(TIME_SEPARATOR)
            if (parts.size == 2) {
                LocalTime.of(parts[0].toInt(), parts[1].toInt())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a LocalDate and LocalTime to epoch milliseconds.
     */
    fun dateAndTimeToMillis(date: LocalDate, time: LocalTime): Long {
        return LocalDateTime
            .of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Converts epoch milliseconds to an OffsetDateTime string (UTC).
     */
    fun millisToOffsetDateTimeString(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atOffset(ZoneOffset.UTC)
            .toString()
    }

    /**
     * Parses an OffsetDateTime string to epoch milliseconds.
     */
    fun offsetDateTimeStringToMillis(dateTimeString: String): Long {
        return try {
            OffsetDateTime.parse(dateTimeString).toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Parses a date string (ISO format) to LocalDate.
     */
    fun parseDateString(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    /**
     * Parses a date string (ISO format) to LocalDate, returning null if invalid.
     */
    fun parseDateStringOrNull(dateString: String?): LocalDate? {
        return try {
            dateString?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
    }
}
