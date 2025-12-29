package com.healthpocket.api.dto

import java.time.OffsetDateTime

/**
 * Sync request DTO - contains all data to synchronize from mobile.
 */
data class SyncRequest(
    val lastSyncTime: OffsetDateTime?,
    val medications: List<MedicationRequest>?,
    val medicationIntakes: List<MedicationIntakeRequest>?,
    val appointments: List<AppointmentRequest>?,
    val healthLogs: List<HealthLogRequest>?,
    val vitalMetrics: List<VitalMetricRequest>?
)

/**
 * Sync response DTO - contains all data synchronized from server.
 */
data class SyncResponse(
    val syncTime: OffsetDateTime,
    val medications: List<MedicationResponse>,
    val medicationIntakes: List<MedicationIntakeResponse>,
    val appointments: List<AppointmentResponse>,
    val healthLogs: List<HealthLogResponse>,
    val vitalMetrics: List<VitalMetricResponse>,
    val syncStats: SyncStats
)

/**
 * Sync statistics.
 */
data class SyncStats(
    val medicationsUploaded: Int,
    val medicationsDownloaded: Int,
    val appointmentsUploaded: Int,
    val appointmentsDownloaded: Int,
    val healthLogsUploaded: Int,
    val healthLogsDownloaded: Int,
    val vitalMetricsUploaded: Int,
    val vitalMetricsDownloaded: Int,
    val conflicts: Int
)

/**
 * API error response DTO.
 */
data class ApiError(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)

