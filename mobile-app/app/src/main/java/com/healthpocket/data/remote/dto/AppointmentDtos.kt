package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentRequest(
    val title: String,
    val description: String? = null,
    @Json(name = "doctorName") val doctorName: String? = null,
    val location: String? = null,
    @Json(name = "appointmentDate") val appointmentDate: String,
    @Json(name = "durationMinutes") val durationMinutes: Int = 30,
    @Json(name = "reminderMinutesBefore") val reminderMinutesBefore: Int = 60,
    @Json(name = "reminderEnabled") val reminderEnabled: Boolean = true,
    val status: String = "SCHEDULED",
    val notes: String? = null,
    @Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class AppointmentResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    @Json(name = "doctorName") val doctorName: String? = null,
    val location: String? = null,
    @Json(name = "appointmentDate") val appointmentDate: String,
    @Json(name = "durationMinutes") val durationMinutes: Int,
    @Json(name = "reminderMinutesBefore") val reminderMinutesBefore: Int,
    @Json(name = "reminderEnabled") val reminderEnabled: Boolean,
    val status: String,
    val notes: String? = null,
    @Json(name = "syncStatus") val syncStatus: String,
    @Json(name = "localId") val localId: String? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

