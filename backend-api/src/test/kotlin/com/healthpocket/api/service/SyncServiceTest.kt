package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(MockKExtension::class)
class SyncServiceTest {

    @MockK
    private lateinit var medicationService: MedicationService

    @MockK
    private lateinit var appointmentService: AppointmentService

    @MockK
    private lateinit var healthLogService: HealthLogService

    @MockK
    private lateinit var vitalMetricService: VitalMetricService

    private lateinit var syncService: SyncService

    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        syncService = SyncService(
            medicationService,
            appointmentService,
            healthLogService,
            vitalMetricService
        )
    }

    @Test
    fun `sync should process medications and return sync response`() {
        // Given
        val medicationRequest = MedicationRequest(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00"),
            startDate = java.time.LocalDate.now()
        )

        val now = OffsetDateTime.now()
        val request = SyncRequest(
            lastSyncTime = null,
            medications = listOf(medicationRequest),
            medicationIntakes = emptyList(),
            appointments = emptyList(),
            healthLogs = emptyList(),
            vitalMetrics = emptyList()
        )

        val medicationResponse = MedicationResponse(
            id = UUID.randomUUID(),
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00"),
            startDate = java.time.LocalDate.now(),
            endDate = null,
            notes = null,
            color = "#4CAF50",
            reminderEnabled = true,
            isActive = true,
            syncStatus = com.healthpocket.api.model.SyncStatus.SYNCED,
            localId = null,
            createdAt = now,
            updatedAt = now
        )

        every { medicationService.createMedication(userId, medicationRequest) } returns medicationResponse
        every { medicationService.getAllMedications(userId) } returns listOf(medicationResponse)
        every { medicationService.getIntakesForMedication(userId, any()) } returns emptyList()
        every { appointmentService.getAllAppointments(userId) } returns emptyList()
        every { healthLogService.getAllHealthLogs(userId) } returns emptyList()
        every { vitalMetricService.getAllVitalMetrics(userId) } returns emptyList()

        // When
        val result = syncService.sync(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.syncStats.medicationsUploaded)
        assertEquals(1, result.syncStats.medicationsDownloaded)
        verify { medicationService.createMedication(userId, medicationRequest) }
    }

    @Test
    fun `sync should process appointments and return sync response`() {
        // Given
        val now = OffsetDateTime.now()
        val appointmentRequest = AppointmentRequest(
            title = "Appointment",
            appointmentDate = now,
            durationMinutes = 30
        )

        val request = SyncRequest(
            lastSyncTime = null,
            medications = emptyList(),
            medicationIntakes = emptyList(),
            appointments = listOf(appointmentRequest),
            healthLogs = emptyList(),
            vitalMetrics = emptyList()
        )

        val appointmentResponse = AppointmentResponse(
            id = UUID.randomUUID(),
            title = "Appointment",
            description = null,
            doctorName = null,
            location = null,
            appointmentDate = now,
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = com.healthpocket.api.model.AppointmentStatus.SCHEDULED,
            notes = null,
            syncStatus = com.healthpocket.api.model.SyncStatus.SYNCED,
            localId = null,
            createdAt = now,
            updatedAt = now
        )

        every { appointmentService.createAppointment(userId, appointmentRequest) } returns appointmentResponse
        every { medicationService.getAllMedications(userId) } returns emptyList()
        every { appointmentService.getAllAppointments(userId) } returns listOf(appointmentResponse)
        every { healthLogService.getAllHealthLogs(userId) } returns emptyList()
        every { vitalMetricService.getAllVitalMetrics(userId) } returns emptyList()

        // When
        val result = syncService.sync(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.syncStats.appointmentsUploaded)
        assertEquals(1, result.syncStats.appointmentsDownloaded)
    }

    @Test
    fun `sync should handle errors gracefully`() {
        // Given
        val medicationRequest = MedicationRequest(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00"),
            startDate = java.time.LocalDate.now()
        )

        val request = SyncRequest(
            lastSyncTime = null,
            medications = listOf(medicationRequest),
            medicationIntakes = emptyList(),
            appointments = emptyList(),
            healthLogs = emptyList(),
            vitalMetrics = emptyList()
        )

        every { medicationService.createMedication(userId, medicationRequest) } throws RuntimeException("Error")
        every { medicationService.getAllMedications(userId) } returns emptyList()
        every { appointmentService.getAllAppointments(userId) } returns emptyList()
        every { healthLogService.getAllHealthLogs(userId) } returns emptyList()
        every { vitalMetricService.getAllVitalMetrics(userId) } returns emptyList()

        // When
        val result = syncService.sync(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(0, result.syncStats.medicationsUploaded)
        assertEquals(1, result.syncStats.conflicts)
    }
}
