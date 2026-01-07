package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthLogRequest(
    @field:Json(name = "logDate") val logDate: String,
    val mood: Int? = null,
    @field:Json(name = "energyLevel") val energyLevel: Int? = null,
    @field:Json(name = "sleepQuality") val sleepQuality: Int? = null,
    @field:Json(name = "sleepHours") val sleepHours: Float? = null,
    val symptoms: List<String>? = null,
    val notes: String? = null,
    @field:Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthLogResponse(
    val id: String,
    @field:Json(name = "logDate") val logDate: String,
    val mood: Int? = null,
    @field:Json(name = "energyLevel") val energyLevel: Int? = null,
    @field:Json(name = "sleepQuality") val sleepQuality: Int? = null,
    @field:Json(name = "sleepHours") val sleepHours: Float? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null,
    @field:Json(name = "syncStatus") val syncStatus: String,
    @field:Json(name = "localId") val localId: String? = null,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "updatedAt") val updatedAt: String
)

