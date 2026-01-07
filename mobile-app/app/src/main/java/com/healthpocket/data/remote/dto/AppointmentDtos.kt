package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentRequest(
    val title: String,
    val description: String? = null,
    @field:Json(name = "doctorName") val doctorName: String? = null,
    val location: String? = null,
    @field:Json(name = "appointmentDate") val appointmentDate: String,
    @field:Json(name = "durationMinutes") val durationMinutes: Int = 30,
    @field:Json(name = "reminderMinutesBefore") val reminderMinutesBefore: Int = 60,
    @field:Json(name = "reminderEnabled") val reminderEnabled: Boolean = true,
    val status: String = "SCHEDULED",
    val notes: String? = null,
    @field:Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class AppointmentResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    @field:Json(name = "doctorName") val doctorName: String? = null,
    val location: String? = null,
    @field:Json(name = "appointmentDate") val appointmentDate: String,
    @field:Json(name = "durationMinutes") val durationMinutes: Int,
    @field:Json(name = "reminderMinutesBefore") val reminderMinutesBefore: Int,
    @field:Json(name = "reminderEnabled") val reminderEnabled: Boolean,
    val status: String,
    val notes: String? = null,
    @field:Json(name = "syncStatus") val syncStatus: String,
    @field:Json(name = "localId") val localId: String? = null,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "updatedAt") val updatedAt: String
)

