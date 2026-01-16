package com.healthpocket.api.service

import com.healthpocket.api.dto.VitalMetricRequest
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.MetricType
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.model.VitalMetric
import com.healthpocket.api.repository.VitalMetricRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(MockKExtension::class)
class VitalMetricServiceTest {

    @MockK
    private lateinit var vitalMetricRepository: VitalMetricRepository

    private lateinit var vitalMetricService: VitalMetricService

    private val userId = UUID.randomUUID()
    private val metricId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        vitalMetricService = VitalMetricService(vitalMetricRepository)
    }

    @Test
    fun `getAllVitalMetrics should return all user metrics`() {
        // Given
        val metrics = listOf(
            createMetric(MetricType.WEIGHT),
            createMetric(MetricType.HEART_RATE)
        )

        every { vitalMetricRepository.findByUserId(userId) } returns metrics

        // When
        val result = vitalMetricService.getAllVitalMetrics(userId)

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `getVitalMetricsByType should return metrics of specific type`() {
        // Given
        val weightMetrics = listOf(createMetric(MetricType.WEIGHT))

        every { vitalMetricRepository.findByUserIdAndMetricTypeOrderByMeasuredAtDesc(userId, MetricType.WEIGHT) } returns weightMetrics

        // When
        val result = vitalMetricService.getVitalMetricsByType(userId, MetricType.WEIGHT)

        // Then
        assertEquals(1, result.size)
        assertEquals(MetricType.WEIGHT, result[0].metricType)
    }

    @Test
    fun `getVitalMetricById should return metric when found`() {
        // Given
        val metric = createMetric(MetricType.WEIGHT)

        every { vitalMetricRepository.findById(metricId) } returns Optional.of(metric)

        // When
        val result = vitalMetricService.getVitalMetricById(userId, metricId)

        // Then
        assertNotNull(result)
        assertEquals(MetricType.WEIGHT, result.metricType)
    }

    @Test
    fun `getVitalMetricById should throw exception when not found`() {
        // Given
        every { vitalMetricRepository.findById(metricId) } returns Optional.empty()

        // When/Then
        assertThrows<ResourceNotFoundException> {
            vitalMetricService.getVitalMetricById(userId, metricId)
        }
    }

    @Test
    fun `createVitalMetric should save and return metric`() {
        // Given
        val now = OffsetDateTime.now()
        val request = VitalMetricRequest(
            metricType = MetricType.WEIGHT,
            value = java.math.BigDecimal("70.5"),
            secondaryValue = null,
            unit = "kg",
            measuredAt = now,
            notes = "Morning weight"
        )

        val savedMetric = VitalMetric(
            id = metricId,
            userId = userId,
            metricType = request.metricType,
            value = request.value,
            secondaryValue = request.secondaryValue,
            unit = request.unit,
            measuredAt = request.measuredAt,
            notes = request.notes,
            syncStatus = SyncStatus.SYNCED
        )

        every { vitalMetricRepository.findByLocalId(any()) } returns null
        every { vitalMetricRepository.save(any()) } returns savedMetric

        // When
        val result = vitalMetricService.createVitalMetric(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(MetricType.WEIGHT, result.metricType)
        assertEquals(0, java.math.BigDecimal("70.5").compareTo(result.value), "Value should be 70.5")

        verify { vitalMetricRepository.save(any()) }
    }

    @Test
    fun `updateVitalMetric should update and return metric`() {
        // Given
        val existingMetric = createMetric(MetricType.WEIGHT)
        val request = VitalMetricRequest(
            metricType = MetricType.WEIGHT,
            value = java.math.BigDecimal("71.0"),
            secondaryValue = null,
            unit = "kg",
            measuredAt = OffsetDateTime.now(),
            notes = "Updated weight"
        )

        every { vitalMetricRepository.findById(metricId) } returns Optional.of(existingMetric)
        every { vitalMetricRepository.save(any()) } returns(existingMetric)

        // When
        val result = vitalMetricService.updateVitalMetric(userId, metricId, request)

        // Then
        assertNotNull(result)
        verify { vitalMetricRepository.save(any()) }
    }

    @Test
    fun `deleteVitalMetric should delete metric when found`() {
        // Given
        val metric = createMetric(MetricType.WEIGHT)

        every { vitalMetricRepository.findById(metricId) } returns Optional.of(metric)
        every { vitalMetricRepository.delete(any()) } returns Unit

        // When
        vitalMetricService.deleteVitalMetric(userId, metricId)

        // Then
        verify { vitalMetricRepository.delete(metric) }
    }

    private fun createMetric(type: MetricType): VitalMetric {
        return VitalMetric(
            id = metricId,
            userId = userId,
            metricType = type,
            value = java.math.BigDecimal("70.5"),
            secondaryValue = null,
            unit = "kg",
            measuredAt = OffsetDateTime.now(),
            notes = "Test metric",
            syncStatus = SyncStatus.SYNCED
        )
    }
}
