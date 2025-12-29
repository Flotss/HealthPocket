package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthLogRequest(
    @Json(name = "logDate") val logDate: String,
    val mood: Int? = null,
    @Json(name = "energyLevel") val energyLevel: Int? = null,
    @Json(name = "sleepQuality") val sleepQuality: Int? = null,
    @Json(name = "sleepHours") val sleepHours: Float? = null,
    val symptoms: List<String>? = null,
    val notes: String? = null,
    @Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthLogResponse(
    val id: String,
    @Json(name = "logDate") val logDate: String,
    val mood: Int? = null,
    @Json(name = "energyLevel") val energyLevel: Int? = null,
    @Json(name = "sleepQuality") val sleepQuality: Int? = null,
    @Json(name = "sleepHours") val sleepHours: Float? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null,
    @Json(name = "syncStatus") val syncStatus: String,
    @Json(name = "localId") val localId: String? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

