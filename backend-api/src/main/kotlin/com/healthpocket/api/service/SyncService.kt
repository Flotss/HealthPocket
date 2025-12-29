package com.healthpocket.api.service

import com.healthpocket.api.dto.*
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
                conflicts++
            }
        }

        // Process incoming medication intakes
        request.medicationIntakes?.forEach { intakeRequest ->
            try {
                medicationService.createIntake(userId, intakeRequest)
            } catch (e: Exception) {
                conflicts++
            }
        }

        // Process incoming appointments
        request.appointments?.forEach { appointmentRequest ->
            try {
                appointmentService.createAppointment(userId, appointmentRequest)
                appointmentsUploaded++
            } catch (e: Exception) {
                conflicts++
            }
        }

        // Process incoming health logs
        request.healthLogs?.forEach { healthLogRequest ->
            try {
                healthLogService.createOrUpdateHealthLog(userId, healthLogRequest)
                healthLogsUploaded++
            } catch (e: Exception) {
                conflicts++
            }
        }

        // Process incoming vital metrics
        request.vitalMetrics?.forEach { vitalMetricRequest ->
            try {
                vitalMetricService.createVitalMetric(userId, vitalMetricRequest)
                vitalMetricsUploaded++
            } catch (e: Exception) {
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

