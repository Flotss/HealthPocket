package com.healthpocket.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthpocket.api.dto.MedicationRequest
import com.healthpocket.api.dto.MedicationResponse
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.service.MedicationService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.boot.test.mock.mockito.MockBean
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
import java.time.LocalDate
import java.util.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class MedicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var medicationService: MedicationService

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
    private val medicationId = UUID.randomUUID()

    @Test
    fun `getAllMedications should return list of medications`() {
        // Given
        val medications = listOf(createMedicationResponse())
        `when`(medicationService.getAllMedications(userId)).thenReturn(medications)

        // When/Then
        mockMvc.perform(
            get("/api/medications")
                .with(user(createUserDetails()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `createMedication should create and return medication`() {
        // Given
        val request = MedicationRequest(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00"),
            startDate = LocalDate.now()
        )
        val response = createMedicationResponse()
        `when`(medicationService.createMedication(any(), any())).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/medications")
                .with(user(createUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `getMedication should return medication by id`() {
        // Given
        val response = createMedicationResponse()
        `when`(medicationService.getMedicationById(userId, medicationId)).thenReturn(response)

        // When/Then
        mockMvc.perform(
            get("/api/medications/$medicationId")
                .with(user(createUserDetails()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `deleteMedication should delete medication`() {
        // Given
        `when`(medicationService.deleteMedication(userId, medicationId)).thenReturn(Unit)

        // When/Then
        mockMvc.perform(
            delete("/api/medications/$medicationId")
                .with(user(createUserDetails()))
        )
            .andExpect(status().isNoContent)
    }

    private fun createUserDetails(): org.springframework.security.core.userdetails.UserDetails {
        return org.springframework.security.core.userdetails.User(
            userId.toString(),
            "password",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }

    private fun createMedicationResponse(): MedicationResponse {
        val now = java.time.OffsetDateTime.now()
        return MedicationResponse(
            id = medicationId,
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00"),
            startDate = LocalDate.now(),
            endDate = null,
            notes = null,
            color = "#4CAF50",
            reminderEnabled = true,
            isActive = true,
            syncStatus = SyncStatus.SYNCED,
            localId = null,
            createdAt = now,
            updatedAt = now
        )
    }
}
