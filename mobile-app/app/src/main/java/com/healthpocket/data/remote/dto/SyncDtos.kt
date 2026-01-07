package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncRequest(
    @field:Json(name = "lastSyncTime") val lastSyncTime: String? = null,
    val medications: List<MedicationRequest>? = null,
    @field:Json(name = "medicationIntakes") val medicationIntakes: List<MedicationIntakeRequest>? = null,
    val appointments: List<AppointmentRequest>? = null,
    @field:Json(name = "healthLogs") val healthLogs: List<HealthLogRequest>? = null,
    @field:Json(name = "vitalMetrics") val vitalMetrics: List<VitalMetricRequest>? = null
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    @field:Json(name = "syncTime") val syncTime: String,
    val medications: List<MedicationResponse>,
    @field:Json(name = "medicationIntakes") val medicationIntakes: List<MedicationIntakeResponse>,
    val appointments: List<AppointmentResponse>,
    @field:Json(name = "healthLogs") val healthLogs: List<HealthLogResponse>,
    @field:Json(name = "vitalMetrics") val vitalMetrics: List<VitalMetricResponse>,
    @field:Json(name = "syncStats") val syncStats: SyncStats
)

@JsonClass(generateAdapter = true)
data class SyncStats(
    @field:Json(name = "medicationsUploaded") val medicationsUploaded: Int,
    @field:Json(name = "medicationsDownloaded") val medicationsDownloaded: Int,
    @field:Json(name = "appointmentsUploaded") val appointmentsUploaded: Int,
    @field:Json(name = "appointmentsDownloaded") val appointmentsDownloaded: Int,
    @field:Json(name = "healthLogsUploaded") val healthLogsUploaded: Int,
    @field:Json(name = "healthLogsDownloaded") val healthLogsDownloaded: Int,
    @field:Json(name = "vitalMetricsUploaded") val vitalMetricsUploaded: Int,
    @field:Json(name = "vitalMetricsDownloaded") val vitalMetricsDownloaded: Int,
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

