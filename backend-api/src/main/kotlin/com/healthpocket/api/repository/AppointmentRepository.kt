package com.healthpocket.api.repository

import com.healthpocket.api.model.Appointment
import com.healthpocket.api.model.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface AppointmentRepository : JpaRepository<Appointment, UUID> {
    fun findByUserId(userId: UUID): List<Appointment>
    fun findByUserIdAndStatus(userId: UUID, status: AppointmentStatus): List<Appointment>
    
    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND a.appointmentDate >= :fromDate ORDER BY a.appointmentDate ASC")
    fun findUpcomingByUserId(userId: UUID, fromDate: OffsetDateTime): List<Appointment>
    
    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId AND a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate ASC")
    fun findByUserIdAndDateRange(userId: UUID, startDate: OffsetDateTime, endDate: OffsetDateTime): List<Appointment>
    
    fun findByLocalId(localId: String): Appointment?
}

