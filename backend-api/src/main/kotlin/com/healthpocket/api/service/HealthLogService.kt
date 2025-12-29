package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.HealthLog
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.HealthLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class HealthLogService(
    private val healthLogRepository: HealthLogRepository
) {

    fun getAllHealthLogs(userId: UUID): List<HealthLogResponse> {
        return healthLogRepository.findByUserIdOrderByLogDateDesc(userId)
            .map { HealthLogResponse.fromEntity(it) }
    }

    fun getHealthLogsInRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<HealthLogResponse> {
        return healthLogRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .map { HealthLogResponse.fromEntity(it) }
    }

    fun getHealthLogByDate(userId: UUID, date: LocalDate): HealthLogResponse? {
        return healthLogRepository.findByUserIdAndLogDate(userId, date)
            ?.let { HealthLogResponse.fromEntity(it) }
    }

    fun getHealthLogById(userId: UUID, logId: UUID): HealthLogResponse {
        val log = findHealthLogByIdAndUser(userId, logId)
        return HealthLogResponse.fromEntity(log)
    }

    @Transactional
    fun createOrUpdateHealthLog(userId: UUID, request: HealthLogRequest): HealthLogResponse {
        // Check if log for this date already exists
        val existingLog = healthLogRepository.findByUserIdAndLogDate(userId, request.logDate)

        val log = existingLog ?: HealthLog(
            userId = userId,
            logDate = request.logDate
        )

        request.mood?.let { log.mood = it }
        request.energyLevel?.let { log.energyLevel = it }
        request.sleepQuality?.let { log.sleepQuality = it }
        request.sleepHours?.let { log.sleepHours = it }
        request.symptoms?.let { log.symptoms = toJsonArray(it) }
        request.notes?.let { log.notes = it }
        request.localId?.let { log.localId = it }
        log.syncStatus = SyncStatus.SYNCED

        val savedLog = healthLogRepository.save(log)
        return HealthLogResponse.fromEntity(savedLog)
    }

    @Transactional
    fun updateHealthLog(userId: UUID, logId: UUID, request: HealthLogRequest): HealthLogResponse {
        val log = findHealthLogByIdAndUser(userId, logId)

        log.logDate = request.logDate
        request.mood?.let { log.mood = it }
        request.energyLevel?.let { log.energyLevel = it }
        request.sleepQuality?.let { log.sleepQuality = it }
        request.sleepHours?.let { log.sleepHours = it }
        request.symptoms?.let { log.symptoms = toJsonArray(it) }
        request.notes?.let { log.notes = it }
        log.syncStatus = SyncStatus.SYNCED

        val savedLog = healthLogRepository.save(log)
        return HealthLogResponse.fromEntity(savedLog)
    }

    @Transactional
    fun deleteHealthLog(userId: UUID, logId: UUID) {
        val log = findHealthLogByIdAndUser(userId, logId)
        healthLogRepository.delete(log)
    }

    private fun findHealthLogByIdAndUser(userId: UUID, logId: UUID): HealthLog {
        val log = healthLogRepository.findById(logId)
            .orElseThrow { ResourceNotFoundException("Health log not found with id: $logId") }

        if (log.userId != userId) {
            throw ResourceNotFoundException("Health log not found")
        }

        return log
    }

    private fun toJsonArray(items: List<String>): String {
        return items.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }
}

