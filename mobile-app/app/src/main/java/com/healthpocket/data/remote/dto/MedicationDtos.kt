package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicationRequest(
    val name: String,
    val dosage: String,
    val frequency: String,
    @Json(name = "scheduleTimes") val scheduleTimes: List<String>,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String? = null,
    val notes: String? = null,
    val color: String = "#4CAF50",
    @Json(name = "reminderEnabled") val reminderEnabled: Boolean = true,
    @Json(name = "isActive") val isActive: Boolean = true,
    @Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class MedicationResponse(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    @Json(name = "scheduleTimes") val scheduleTimes: List<String>,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String? = null,
    val notes: String? = null,
    val color: String,
    @Json(name = "reminderEnabled") val reminderEnabled: Boolean,
    @Json(name = "isActive") val isActive: Boolean,
    @Json(name = "syncStatus") val syncStatus: String,
    @Json(name = "localId") val localId: String? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class MedicationIntakeRequest(
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "scheduledTime") val scheduledTime: String,
    @Json(name = "takenTime") val takenTime: String? = null,
    val status: String = "PENDING",
    val notes: String? = null,
    @Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class MedicationIntakeResponse(
    val id: String,
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "scheduledTime") val scheduledTime: String,
    @Json(name = "takenTime") val takenTime: String? = null,
    val status: String,
    val notes: String? = null,
    @Json(name = "syncStatus") val syncStatus: String,
    @Json(name = "localId") val localId: String? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class UpdateIntakeStatusRequest(
    val status: String,
    @Json(name = "takenTime") val takenTime: String? = null,
    val notes: String? = null
)

