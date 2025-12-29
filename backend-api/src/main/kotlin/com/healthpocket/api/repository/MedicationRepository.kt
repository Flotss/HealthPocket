package com.healthpocket.api.repository

import com.healthpocket.api.model.Medication
import com.healthpocket.api.model.SyncStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MedicationRepository : JpaRepository<Medication, UUID> {
    fun findByUserId(userId: UUID): List<Medication>
    fun findByUserIdAndIsActive(userId: UUID, isActive: Boolean): List<Medication>
    fun findByUserIdAndSyncStatus(userId: UUID, syncStatus: SyncStatus): List<Medication>
    fun findByLocalId(localId: String): Medication?
}

