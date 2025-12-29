package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncRequest(
    @Json(name = "lastSyncTime") val lastSyncTime: String? = null,
    val medications: List<MedicationRequest>? = null,
    @Json(name = "medicationIntakes") val medicationIntakes: List<MedicationIntakeRequest>? = null,
    val appointments: List<AppointmentRequest>? = null,
    @Json(name = "healthLogs") val healthLogs: List<HealthLogRequest>? = null,
    @Json(name = "vitalMetrics") val vitalMetrics: List<VitalMetricRequest>? = null
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    @Json(name = "syncTime") val syncTime: String,
    val medications: List<MedicationResponse>,
    @Json(name = "medicationIntakes") val medicationIntakes: List<MedicationIntakeResponse>,
    val appointments: List<AppointmentResponse>,
    @Json(name = "healthLogs") val healthLogs: List<HealthLogResponse>,
    @Json(name = "vitalMetrics") val vitalMetrics: List<VitalMetricResponse>,
    @Json(name = "syncStats") val syncStats: SyncStats
)

@JsonClass(generateAdapter = true)
data class SyncStats(
    @Json(name = "medicationsUploaded") val medicationsUploaded: Int,
    @Json(name = "medicationsDownloaded") val medicationsDownloaded: Int,
    @Json(name = "appointmentsUploaded") val appointmentsUploaded: Int,
    @Json(name = "appointmentsDownloaded") val appointmentsDownloaded: Int,
    @Json(name = "healthLogsUploaded") val healthLogsUploaded: Int,
    @Json(name = "healthLogsDownloaded") val healthLogsDownloaded: Int,
    @Json(name = "vitalMetricsUploaded") val vitalMetricsUploaded: Int,
    @Json(name = "vitalMetricsDownloaded") val vitalMetricsDownloaded: Int,
    val conflicts: Int
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)

