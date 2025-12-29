package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.MetricType
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.model.VitalMetric
import com.healthpocket.api.repository.VitalMetricRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VitalMetricService(
    private val vitalMetricRepository: VitalMetricRepository
) {

    fun getAllVitalMetrics(userId: UUID): List<VitalMetricResponse> {
        return vitalMetricRepository.findByUserId(userId)
            .map { VitalMetricResponse.fromEntity(it) }
    }

    fun getVitalMetricsByType(userId: UUID, metricType: MetricType): List<VitalMetricResponse> {
        return vitalMetricRepository.findByUserIdAndMetricTypeOrderByMeasuredAtDesc(userId, metricType)
            .map { VitalMetricResponse.fromEntity(it) }
    }

    fun getVitalMetricsInRange(userId: UUID, startDate: OffsetDateTime, endDate: OffsetDateTime): List<VitalMetricResponse> {
        return vitalMetricRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .map { VitalMetricResponse.fromEntity(it) }
    }

    fun getVitalMetricsSummary(userId: UUID): VitalMetricsSummary {
        return VitalMetricsSummary(
            latestWeight = vitalMetricRepository.findLatestByUserIdAndMetricType(userId, MetricType.WEIGHT)
                ?.let { VitalMetricResponse.fromEntity(it) },
            latestBloodPressure = vitalMetricRepository.findLatestByUserIdAndMetricType(userId, MetricType.BLOOD_PRESSURE)
                ?.let { VitalMetricResponse.fromEntity(it) },
            latestBloodGlucose = vitalMetricRepository.findLatestByUserIdAndMetricType(userId, MetricType.BLOOD_GLUCOSE)
                ?.let { VitalMetricResponse.fromEntity(it) },
            latestHeartRate = vitalMetricRepository.findLatestByUserIdAndMetricType(userId, MetricType.HEART_RATE)
                ?.let { VitalMetricResponse.fromEntity(it) },
            latestTemperature = vitalMetricRepository.findLatestByUserIdAndMetricType(userId, MetricType.TEMPERATURE)
                ?.let { VitalMetricResponse.fromEntity(it) }
        )
    }

    fun getVitalMetricById(userId: UUID, metricId: UUID): VitalMetricResponse {
        val metric = findVitalMetricByIdAndUser(userId, metricId)
        return VitalMetricResponse.fromEntity(metric)
    }

    @Transactional
    fun createVitalMetric(userId: UUID, request: VitalMetricRequest): VitalMetricResponse {
        // Check if metric with same localId already exists
        request.localId?.let { localId ->
            vitalMetricRepository.findByLocalId(localId)?.let {
                return updateVitalMetricByLocalId(userId, localId, request)
            }
        }

        val metric = VitalMetric(
            userId = userId,
            metricType = request.metricType,
            value = request.value,
            secondaryValue = request.secondaryValue,
            unit = request.unit,
            measuredAt = request.measuredAt,
            notes = request.notes,
            syncStatus = SyncStatus.SYNCED,
            localId = request.localId
        )

        val savedMetric = vitalMetricRepository.save(metric)
        return VitalMetricResponse.fromEntity(savedMetric)
    }

    @Transactional
    fun updateVitalMetric(userId: UUID, metricId: UUID, request: VitalMetricRequest): VitalMetricResponse {
        val metric = findVitalMetricByIdAndUser(userId, metricId)

        metric.metricType = request.metricType
        metric.value = request.value
        metric.secondaryValue = request.secondaryValue
        metric.unit = request.unit
        metric.measuredAt = request.measuredAt
        metric.notes = request.notes
        metric.syncStatus = SyncStatus.SYNCED

        val savedMetric = vitalMetricRepository.save(metric)
        return VitalMetricResponse.fromEntity(savedMetric)
    }

    private fun updateVitalMetricByLocalId(userId: UUID, localId: String, request: VitalMetricRequest): VitalMetricResponse {
        val metric = vitalMetricRepository.findByLocalId(localId)
            ?: throw ResourceNotFoundException("Vital metric not found with localId: $localId")

        if (metric.userId != userId) {
            throw ResourceNotFoundException("Vital metric not found")
        }

        metric.metricType = request.metricType
        metric.value = request.value
        metric.secondaryValue = request.secondaryValue
        metric.unit = request.unit
        metric.measuredAt = request.measuredAt
        metric.notes = request.notes
        metric.syncStatus = SyncStatus.SYNCED

        val savedMetric = vitalMetricRepository.save(metric)
        return VitalMetricResponse.fromEntity(savedMetric)
    }

    @Transactional
    fun deleteVitalMetric(userId: UUID, metricId: UUID) {
        val metric = findVitalMetricByIdAndUser(userId, metricId)
        vitalMetricRepository.delete(metric)
    }

    private fun findVitalMetricByIdAndUser(userId: UUID, metricId: UUID): VitalMetric {
        val metric = vitalMetricRepository.findById(metricId)
            .orElseThrow { ResourceNotFoundException("Vital metric not found with id: $metricId") }

        if (metric.userId != userId) {
            throw ResourceNotFoundException("Vital metric not found")
        }

        return metric
    }
}

