package com.healthpocket.data.remote.dto

import com.healthpocket.ui.theme.AppColors
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicationRequest(
    val name: String,
    val dosage: String,
    val frequency: String,
    @field:Json(name = "scheduleTimes") val scheduleTimes: List<String>,
    @field:Json(name = "startDate") val startDate: String,
    @field:Json(name = "endDate") val endDate: String? = null,
    val notes: String? = null,
    val color: String = AppColors.MEDICATION_DEFAULT,
    @field:Json(name = "reminderEnabled") val reminderEnabled: Boolean = true,
    @field:Json(name = "isActive") val isActive: Boolean = true,
    @field:Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class MedicationResponse(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    @field:Json(name = "scheduleTimes") val scheduleTimes: List<String>,
    @field:Json(name = "startDate") val startDate: String,
    @field:Json(name = "endDate") val endDate: String? = null,
    val notes: String? = null,
    val color: String,
    @field:Json(name = "reminderEnabled") val reminderEnabled: Boolean,
    @field:Json(name = "isActive") val isActive: Boolean,
    @field:Json(name = "syncStatus") val syncStatus: String,
    @field:Json(name = "localId") val localId: String? = null,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class MedicationIntakeRequest(
    @field:Json(name = "medicationId") val medicationId: String,
    @field:Json(name = "scheduledTime") val scheduledTime: String,
    @field:Json(name = "takenTime") val takenTime: String? = null,
    val status: String = "PENDING",
    val notes: String? = null,
    @field:Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class MedicationIntakeResponse(
    val id: String,
    @field:Json(name = "medicationId") val medicationId: String,
    @field:Json(name = "scheduledTime") val scheduledTime: String,
    @field:Json(name = "takenTime") val takenTime: String? = null,
    val status: String,
    val notes: String? = null,
    @field:Json(name = "syncStatus") val syncStatus: String,
    @field:Json(name = "localId") val localId: String? = null,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class UpdateIntakeStatusRequest(
    val status: String,
    @field:Json(name = "takenTime") val takenTime: String? = null,
    val notes: String? = null
)

