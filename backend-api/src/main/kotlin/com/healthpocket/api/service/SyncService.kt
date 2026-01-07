package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Service for handling data synchronization between mobile app and backend.
 */
@Service
class SyncService(
    private val medicationService: MedicationService,
    private val appointmentService: AppointmentService,
    private val healthLogService: HealthLogService,
    private val vitalMetricService: VitalMetricService
) {

    private val logger = LoggerFactory.getLogger(SyncService::class.java)

    /**
     * Synchronize data from mobile app.
     * Receives pending changes from mobile and returns all server data.
     */
    @Transactional
    fun sync(userId: UUID, request: SyncRequest): SyncResponse {
        var medicationsUploaded = 0
        var appointmentsUploaded = 0
        var healthLogsUploaded = 0
        var vitalMetricsUploaded = 0
        var conflicts = 0

        // Process incoming medications
        request.medications?.forEach { medicationRequest ->
            try {
                medicationService.createMedication(userId, medicationRequest)
                medicationsUploaded++
            } catch (e: Exception) {
                logger.error("Failed to sync medication for user $userId. Data: $medicationRequest", e)
                conflicts++
            }
        }

        // Process incoming medication intakes
        request.medicationIntakes?.forEach { intakeRequest ->
            try {
                medicationService.createIntake(userId, intakeRequest)
            } catch (e: Exception) {
                logger.error("Failed to sync medication intake for user $userId. Data: $intakeRequest", e)
                conflicts++
            }
        }

        // Process incoming appointments
        request.appointments?.forEach { appointmentRequest ->
            try {
                appointmentService.createAppointment(userId, appointmentRequest)
                appointmentsUploaded++
            } catch (e: Exception) {
                logger.error("Failed to sync appointment for user $userId. Data: $appointmentRequest", e)
                conflicts++
            }
        }

        // Process incoming health logs
        request.healthLogs?.forEach { healthLogRequest ->
            try {
                healthLogService.createOrUpdateHealthLog(userId, healthLogRequest)
                healthLogsUploaded++
            } catch (e: Exception) {
                logger.error("Failed to sync health log for user $userId. Data: $healthLogRequest", e)
                conflicts++
            }
        }

        // Process incoming vital metrics
        request.vitalMetrics?.forEach { vitalMetricRequest ->
            try {
                vitalMetricService.createVitalMetric(userId, vitalMetricRequest)
                vitalMetricsUploaded++
            } catch (e: Exception) {
                logger.error("Failed to sync vital metric for user $userId. Data: $vitalMetricRequest", e)
                conflicts++
            }
        }

        // Return all data to sync down to mobile
        val medications = medicationService.getAllMedications(userId)
        val appointments = appointmentService.getAllAppointments(userId)
        val healthLogs = healthLogService.getAllHealthLogs(userId)
        val vitalMetrics = vitalMetricService.getAllVitalMetrics(userId)

        // Get intakes for all medications
        val allIntakes = medications.flatMap { medication ->
            try {
                medicationService.getIntakesForMedication(userId, medication.id)
            } catch (e: Exception) {
                logger.error("Failed to fetch intakes for medication ${medication.id}", e)
                emptyList()
            }
        }

        return SyncResponse(
            syncTime = OffsetDateTime.now(),
            medications = medications,
            medicationIntakes = allIntakes,
            appointments = appointments,
            healthLogs = healthLogs,
            vitalMetrics = vitalMetrics,
            syncStats = SyncStats(
                medicationsUploaded = medicationsUploaded,
                medicationsDownloaded = medications.size,
                appointmentsUploaded = appointmentsUploaded,
                appointmentsDownloaded = appointments.size,
                healthLogsUploaded = healthLogsUploaded,
                healthLogsDownloaded = healthLogs.size,
                vitalMetricsUploaded = vitalMetricsUploaded,
                vitalMetricsDownloaded = vitalMetrics.size,
                conflicts = conflicts
            )
        )
    }
}
