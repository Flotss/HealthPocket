package com.healthpocket.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.healthpocket.api.dto.VitalMetricRequest
import com.healthpocket.api.dto.VitalMetricResponse
import com.healthpocket.api.model.MetricType
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.service.VitalMetricService
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
import java.time.OffsetDateTime
import java.util.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import com.healthpocket.api.repository.AppointmentRepository
import com.healthpocket.api.repository.HealthLogRepository
import com.healthpocket.api.repository.MedicationRepository
import com.healthpocket.api.repository.UserRepository
import com.healthpocket.api.repository.VitalMetricRepository

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class VitalMetricControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var vitalMetricService: VitalMetricService

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
    private val metricId = UUID.randomUUID()

    @Test
    fun `getAllVitalMetrics should return list of metrics`() {
        // Given
        val metrics = listOf(createMetricResponse())
        `when`(vitalMetricService.getAllVitalMetrics(userId)).thenReturn(metrics)

        // When/Then
        mockMvc.perform(
            get("/api/vitals")
                .with(user(createUserDetails()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `createVitalMetric should create and return metric`() {
        // Given
        val request = VitalMetricRequest(
            metricType = MetricType.WEIGHT,
            value = java.math.BigDecimal("70.5"),
            unit = "kg",
            measuredAt = OffsetDateTime.now()
        )
        val response = createMetricResponse()
        `when`(vitalMetricService.createVitalMetric(any(), any())).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/vitals")
                .with(user(createUserDetails()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `getVitalMetric should return metric by id`() {
        // Given
        val response = createMetricResponse()
        `when`(vitalMetricService.getVitalMetricById(userId, metricId)).thenReturn(response)

        // When/Then
        mockMvc.perform(
            get("/api/vitals/$metricId")
                .with(user(createUserDetails()))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `deleteVitalMetric should delete metric`() {
        // Given
        `when`(vitalMetricService.deleteVitalMetric(userId, metricId)).thenReturn(Unit)

        // When/Then
        mockMvc.perform(
            delete("/api/vitals/$metricId")
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

    private fun createMetricResponse(): VitalMetricResponse {
        val now = OffsetDateTime.now()
        return VitalMetricResponse(
            id = metricId,
            metricType = MetricType.WEIGHT,
            value = java.math.BigDecimal("70.5"),
            secondaryValue = null,
            unit = "kg",
            measuredAt = now,
            notes = null,
            syncStatus = SyncStatus.SYNCED,
            localId = null,
            createdAt = now,
            updatedAt = now
        )
    }
}
