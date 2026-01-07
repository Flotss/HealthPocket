package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VitalMetricRequest(
    @field:Json(name = "metricType") val metricType: String,
    val value: Float,
    @field:Json(name = "secondaryValue") val secondaryValue: Float? = null,
    val unit: String,
    @field:Json(name = "measuredAt") val measuredAt: String,
    val notes: String? = null,
    @field:Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class VitalMetricResponse(
    val id: String,
    @field:Json(name = "metricType") val metricType: String,
    val value: Float,
    @field:Json(name = "secondaryValue") val secondaryValue: Float? = null,
    val unit: String,
    @field:Json(name = "measuredAt") val measuredAt: String,
    val notes: String? = null,
    @field:Json(name = "syncStatus") val syncStatus: String,
    @field:Json(name = "localId") val localId: String? = null,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class VitalMetricsSummary(
    @field:Json(name = "latestWeight") val latestWeight: VitalMetricResponse? = null,
    @field:Json(name = "latestBloodPressure") val latestBloodPressure: VitalMetricResponse? = null,
    @field:Json(name = "latestBloodGlucose") val latestBloodGlucose: VitalMetricResponse? = null,
    @field:Json(name = "latestHeartRate") val latestHeartRate: VitalMetricResponse? = null,
    @field:Json(name = "latestTemperature") val latestTemperature: VitalMetricResponse? = null
)

