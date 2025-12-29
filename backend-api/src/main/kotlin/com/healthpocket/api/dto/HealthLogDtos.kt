package com.healthpocket.api.dto

import com.healthpocket.api.model.HealthLog
import com.healthpocket.api.model.SyncStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Health log request DTO.
 */
data class HealthLogRequest(
    @field:NotNull(message = "Log date is required")
    val logDate: LocalDate,

    @field:Min(1) @field:Max(5)
    val mood: Int? = null,

    @field:Min(1) @field:Max(5)
    val energyLevel: Int? = null,

    @field:Min(1) @field:Max(5)
    val sleepQuality: Int? = null,

    val sleepHours: BigDecimal? = null,
    val symptoms: List<String>? = null,
    val notes: String? = null,
    val localId: String? = null
)

/**
 * Health log response DTO.
 */
data class HealthLogResponse(
    val id: UUID,
    val logDate: LocalDate,
    val mood: Int?,
    val energyLevel: Int?,
    val sleepQuality: Int?,
    val sleepHours: BigDecimal?,
    val symptoms: List<String>,
    val notes: String?,
    val syncStatus: SyncStatus,
    val localId: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromEntity(log: HealthLog): HealthLogResponse {
            return HealthLogResponse(
                id = log.id!!,
                logDate = log.logDate,
                mood = log.mood,
                energyLevel = log.energyLevel,
                sleepQuality = log.sleepQuality,
                sleepHours = log.sleepHours,
                symptoms = parseSymptoms(log.symptoms),
                notes = log.notes,
                syncStatus = log.syncStatus,
                localId = log.localId,
                createdAt = log.createdAt,
                updatedAt = log.updatedAt
            )
        }

        private fun parseSymptoms(json: String?): List<String> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

