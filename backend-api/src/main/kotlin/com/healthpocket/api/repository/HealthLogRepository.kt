package com.healthpocket.api.repository

import com.healthpocket.api.model.HealthLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface HealthLogRepository : JpaRepository<HealthLog, UUID> {
    fun findByUserId(userId: UUID): List<HealthLog>
    fun findByUserIdAndLogDate(userId: UUID, logDate: LocalDate): HealthLog?
    
    @Query("SELECT h FROM HealthLog h WHERE h.userId = :userId AND h.logDate BETWEEN :startDate AND :endDate ORDER BY h.logDate DESC")
    fun findByUserIdAndDateRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<HealthLog>
    
    @Query("SELECT h FROM HealthLog h WHERE h.userId = :userId ORDER BY h.logDate DESC")
    fun findByUserIdOrderByLogDateDesc(userId: UUID): List<HealthLog>
    
    fun findByLocalId(localId: String): HealthLog?
}

