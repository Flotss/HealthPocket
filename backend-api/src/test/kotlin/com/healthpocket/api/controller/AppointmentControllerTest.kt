package com.healthpocket.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthpocket.api.dto.AppointmentRequest
import com.healthpocket.api.dto.AppointmentResponse
import com.healthpocket.api.model.AppointmentStatus
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.AppointmentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.healthpocket.api.repository.AppointmentRepository
import com.healthpocket.api.repository.HealthLogRepository
import com.healthpocket.api.repository.MedicationRepository
import com.healthpocket.api.repository.UserRepository
import com.healthpocket.api.repository.VitalMetricRepository
import java.time.OffsetDateTime
import java.util.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class AppointmentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var appointmentService: AppointmentService

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var appointmentRepository: AppointmentRepository

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var healthLogRepository: HealthLogRepository

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var medicationRepository: MedicationRepository

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var userRepository: UserRepository

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var vitalMetricRepository: VitalMetricRepository

    private val userId = UUID.randomUUID()
    private val appointmentId = UUID.randomUUID()

    @Test
    fun `getAllAppointments should return list of appointments`() {
        // Given
        val appointments = listOf(
            createAppointmentResponse()
        )
        `when`(appointmentService.getAllAppointments(userId)).thenReturn(appointments)

        // When/Then
        mockMvc.perform(
            get("/api/appointments")
                .with(user(UserPrincipal(userId, "test@example.com")))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(appointmentId.toString()))
    }

    @Test
    fun `getAppointment should return appointment by id`() {
        // Given
        val appointment = createAppointmentResponse()
        `when`(appointmentService.getAppointmentById(appointmentId, userId)).thenReturn(appointment)

        // When/Then
        mockMvc.perform(
            get("/api/appointments/{id}", appointmentId)
                .with(user(UserPrincipal(userId, "test@example.com")))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(appointmentId.toString()))
    }

    @Test
    fun `createAppointment should create and return appointment`() {
        // Given
        val request = createAppointmentRequest()
        val response = createAppointmentResponse()
        `when`(appointmentService.createAppointment(any(), any())).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/appointments")
                .with(user(UserPrincipal(userId, "test@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(appointmentId.toString()))
    }

    @Test
    fun `deleteAppointment should delete appointment`() {
        // Given
        `when`(appointmentService.deleteAppointment(appointmentId, userId)).thenReturn(Unit)

        // When/Then
        mockMvc.perform(
            delete("/api/appointments/{id}", appointmentId)
                .with(user(UserPrincipal(userId, "test@example.com")))
        )
            .andExpect(status().isNoContent)
    }

    private fun createAppointmentRequest(): AppointmentRequest {
        return AppointmentRequest(
            title = "Test Appointment",
            description = "Test Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = OffsetDateTime.now(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            notes = "Test notes",
            localId = "local-123",
            syncStatus = SyncStatus.PENDING
        )
    }

    private fun createAppointmentResponse(): AppointmentResponse {
        return AppointmentResponse(
            id = appointmentId.toString(),
            title = "Test Appointment",
            description = "Test Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = OffsetDateTime.now(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            notes = "Test notes",
            status = AppointmentStatus.SCHEDULED,
            localId = "local-123",
            syncStatus = SyncStatus.SYNCED,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
    }
}