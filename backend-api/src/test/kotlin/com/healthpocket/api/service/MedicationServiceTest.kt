package com.healthpocket.api.service

import com.healthpocket.api.dto.MedicationRequest
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.Medication
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.MedicationIntakeRepository
import com.healthpocket.api.repository.MedicationRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class MedicationServiceTest {

    @MockK
    private lateinit var medicationRepository: MedicationRepository

    @MockK
    private lateinit var medicationIntakeRepository: MedicationIntakeRepository

    private lateinit var medicationService: MedicationService

    private val userId = UUID.randomUUID()
    private val medicationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        medicationService = MedicationService(medicationRepository, medicationIntakeRepository)
    }

    @Test
    fun `getAllMedications should return all user medications`() {
        // Given
        val medications = listOf(
            createMedication("Aspirin"),
            createMedication("Ibuprofen")
        )

        every { medicationRepository.findByUserId(userId) } returns medications

        // When
        val result = medicationService.getAllMedications(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Aspirin", result[0].name)
        assertEquals("Ibuprofen", result[1].name)
    }

    @Test
    fun `getActiveMedications should return only active medications`() {
        // Given
        val activeMedication = createMedication("Aspirin", isActive = true)

        every { medicationRepository.findByUserIdAndIsActive(userId, true) } returns listOf(activeMedication)

        // When
        val result = medicationService.getActiveMedications(userId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Aspirin", result[0].name)
    }

    @Test
    fun `getMedicationById should return medication when found`() {
        // Given
        val medication = createMedication("Aspirin")

        every { medicationRepository.findById(medicationId) } returns Optional.of(medication)

        // When
        val result = medicationService.getMedicationById(userId, medicationId)

        // Then
        assertNotNull(result)
        assertEquals("Aspirin", result.name)
    }

    @Test
    fun `getMedicationById should throw exception when not found`() {
        // Given
        every { medicationRepository.findById(medicationId) } returns Optional.empty()

        // When/Then
        assertThrows<ResourceNotFoundException> {
            medicationService.getMedicationById(userId, medicationId)
        }
    }

    @Test
    fun `getMedicationById should throw exception when medication belongs to different user`() {
        // Given
        val differentUserId = UUID.randomUUID()
        val medication = Medication(
            id = medicationId,
            userId = differentUserId,
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "[\"08:00\"]",
            startDate = LocalDate.now()
        )

        every { medicationRepository.findById(medicationId) } returns Optional.of(medication)

        // When/Then
        assertThrows<ResourceNotFoundException> {
            medicationService.getMedicationById(userId, medicationId)
        }
    }

    @Test
    fun `createMedication should save and return medication`() {
        // Given
        val request = MedicationRequest(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00", "20:00"),
            startDate = LocalDate.now()
        )

        val savedMedication = Medication(
            id = medicationId,
            userId = userId,
            name = request.name,
            dosage = request.dosage,
            frequency = request.frequency,
            scheduleTimes = "[\"08:00\", \"20:00\"]",
            startDate = request.startDate,
            syncStatus = SyncStatus.SYNCED
        )

        every { medicationRepository.save(any()) } returns savedMedication

        // When
        val result = medicationService.createMedication(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Aspirin", result.name)
        assertEquals("500mg", result.dosage)
        assertEquals(SyncStatus.SYNCED, result.syncStatus)

        verify { medicationRepository.save(any()) }
    }

    @Test
    fun `deleteMedication should delete medication when found`() {
        // Given
        val medication = createMedication("Aspirin")

        every { medicationRepository.findById(medicationId) } returns Optional.of(medication)
        every { medicationRepository.delete(medication) } returns Unit

        // When
        medicationService.deleteMedication(userId, medicationId)

        // Then
        verify { medicationRepository.delete(medication) }
    }

    private fun createMedication(name: String, isActive: Boolean = true): Medication {
        return Medication(
            id = medicationId,
            userId = userId,
            name = name,
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "[\"08:00\"]",
            startDate = LocalDate.now(),
            isActive = isActive,
            syncStatus = SyncStatus.SYNCED
        )
    }
}

