package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VitalMetricRequest(
    @Json(name = "metricType") val metricType: String,
    val value: Float,
    @Json(name = "secondaryValue") val secondaryValue: Float? = null,
    val unit: String,
    @Json(name = "measuredAt") val measuredAt: String,
    val notes: String? = null,
    @Json(name = "localId") val localId: String? = null
)

@JsonClass(generateAdapter = true)
data class VitalMetricResponse(
    val id: String,
    @Json(name = "metricType") val metricType: String,
    val value: Float,
    @Json(name = "secondaryValue") val secondaryValue: Float? = null,
    val unit: String,
    @Json(name = "measuredAt") val measuredAt: String,
    val notes: String? = null,
    @Json(name = "syncStatus") val syncStatus: String,
    @Json(name = "localId") val localId: String? = null,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class VitalMetricsSummary(
    @Json(name = "latestWeight") val latestWeight: VitalMetricResponse? = null,
    @Json(name = "latestBloodPressure") val latestBloodPressure: VitalMetricResponse? = null,
    @Json(name = "latestBloodGlucose") val latestBloodGlucose: VitalMetricResponse? = null,
    @Json(name = "latestHeartRate") val latestHeartRate: VitalMetricResponse? = null,
    @Json(name = "latestTemperature") val latestTemperature: VitalMetricResponse? = null
)

