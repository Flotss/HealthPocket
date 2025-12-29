package com.healthpocket.api.repository

import com.healthpocket.api.model.MetricType
import com.healthpocket.api.model.VitalMetric
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface VitalMetricRepository : JpaRepository<VitalMetric, UUID> {
    fun findByUserId(userId: UUID): List<VitalMetric>
    fun findByUserIdAndMetricType(userId: UUID, metricType: MetricType): List<VitalMetric>
    
    @Query("SELECT v FROM VitalMetric v WHERE v.userId = :userId AND v.measuredAt BETWEEN :startDate AND :endDate ORDER BY v.measuredAt DESC")
    fun findByUserIdAndDateRange(userId: UUID, startDate: OffsetDateTime, endDate: OffsetDateTime): List<VitalMetric>
    
    @Query("SELECT v FROM VitalMetric v WHERE v.userId = :userId AND v.metricType = :metricType ORDER BY v.measuredAt DESC")
    fun findByUserIdAndMetricTypeOrderByMeasuredAtDesc(userId: UUID, metricType: MetricType): List<VitalMetric>
    
    @Query("SELECT v FROM VitalMetric v WHERE v.userId = :userId AND v.metricType = :metricType ORDER BY v.measuredAt DESC LIMIT 1")
    fun findLatestByUserIdAndMetricType(userId: UUID, metricType: MetricType): VitalMetric?
    
    fun findByLocalId(localId: String): VitalMetric?
}

