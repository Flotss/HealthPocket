package com.healthpocket.api.repository

import com.healthpocket.api.model.MedicationIntake
import com.healthpocket.api.model.IntakeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface MedicationIntakeRepository : JpaRepository<MedicationIntake, UUID> {
    fun findByUserId(userId: UUID): List<MedicationIntake>
    fun findByMedicationId(medicationId: UUID): List<MedicationIntake>
    
    @Query("SELECT i FROM MedicationIntake i WHERE i.userId = :userId AND i.scheduledTime BETWEEN :startDate AND :endDate")
    fun findByUserIdAndScheduledTimeBetween(
        userId: UUID,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime
    ): List<MedicationIntake>
    
    fun findByUserIdAndStatus(userId: UUID, status: IntakeStatus): List<MedicationIntake>
    fun findByLocalId(localId: String): MedicationIntake?
}

