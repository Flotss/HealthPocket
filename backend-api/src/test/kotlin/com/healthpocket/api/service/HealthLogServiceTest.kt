package com.healthpocket.api.service

import com.healthpocket.api.dto.HealthLogRequest
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.HealthLog
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.HealthLogRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(MockKExtension::class)
class HealthLogServiceTest {

    @MockK
    private lateinit var healthLogRepository: HealthLogRepository

    private lateinit var healthLogService: HealthLogService

    private val userId = UUID.randomUUID()
    private val logId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        healthLogService = HealthLogService(healthLogRepository)
    }

    @Test
    fun `getAllHealthLogs should return all user logs`() {
        // Given
        val logs = listOf(
            createHealthLog(LocalDate.now()),
            createHealthLog(LocalDate.now().minusDays(1))
        )

        every { healthLogRepository.findByUserIdOrderByLogDateDesc(userId) } returns logs

        // When
        val result = healthLogService.getAllHealthLogs(userId)

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `getHealthLogByDate should return log when found`() {
        // Given
        val date = LocalDate.now()
        val log = createHealthLog(date)

        every { healthLogRepository.findByUserIdAndLogDate(userId, date) } returns log

        // When
        val result = healthLogService.getHealthLogByDate(userId, date)

        // Then
        assertNotNull(result)
        assertEquals(date, result?.logDate)
    }

    @Test
    fun `getHealthLogById should return log when found`() {
        // Given
        val log = createHealthLog(LocalDate.now())

        every { healthLogRepository.findById(logId) } returns Optional.of(log)

        // When
        val result = healthLogService.getHealthLogById(userId, logId)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `getHealthLogById should throw exception when not found`() {
        // Given
        every { healthLogRepository.findById(logId) } returns Optional.empty()

        // When/Then
        assertThrows<ResourceNotFoundException> {
            healthLogService.getHealthLogById(userId, logId)
        }
    }

    @Test
    fun `createOrUpdateHealthLog should create new log when none exists`() {
        // Given
        val date = LocalDate.now()
        val request = HealthLogRequest(
            logDate = date,
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = java.math.BigDecimal("7.5"),
            symptoms = listOf("Headache"),
            notes = "Feeling good"
        )

        val savedLog = HealthLog(
            id = logId,
            userId = userId,
            logDate = date,
            mood = request.mood,
            energyLevel = request.energyLevel,
            sleepQuality = request.sleepQuality,
            sleepHours = request.sleepHours,
            symptoms = "[\"Headache\"]",
            notes = request.notes,
            syncStatus = SyncStatus.SYNCED
        )

        every { healthLogRepository.findByUserIdAndLogDate(userId, date) } returns null
        every { healthLogRepository.save(any()) } returns savedLog

        // When
        val result = healthLogService.createOrUpdateHealthLog(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(5, result.mood)
        verify { healthLogRepository.save(any()) }
    }

    @Test
    fun `createOrUpdateHealthLog should update existing log`() {
        // Given
        val date = LocalDate.now()
        val existingLog = createHealthLog(date)
        val request = HealthLogRequest(
            logDate = date,
            mood = 4,
            energyLevel = 3
        )

        every { healthLogRepository.findByUserIdAndLogDate(userId, date) } returns existingLog
        every { healthLogRepository.save(any()) } returns existingLog

        // When
        val result = healthLogService.createOrUpdateHealthLog(userId, request)

        // Then
        assertNotNull(result)
        verify { healthLogRepository.save(any()) }
    }

    @Test
    fun `deleteHealthLog should delete log when found`() {
        // Given
        val log = createHealthLog(LocalDate.now())

        every { healthLogRepository.findById(logId) } returns Optional.of(log)
        every { healthLogRepository.delete(any()) } returns Unit

        // When
        healthLogService.deleteHealthLog(userId, logId)

        // Then
        verify { healthLogRepository.delete(log) }
    }

    private fun createHealthLog(date: LocalDate): HealthLog {
        return HealthLog(
            id = logId,
            userId = userId,
            logDate = date,
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = java.math.BigDecimal("7.5"),
            symptoms = "[\"Headache\"]",
            notes = "Test log",
            syncStatus = SyncStatus.SYNCED
        )
    }
}
