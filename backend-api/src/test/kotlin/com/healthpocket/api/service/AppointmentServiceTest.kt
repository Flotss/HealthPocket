package com.healthpocket.api.service

import com.healthpocket.api.dto.AppointmentRequest
import com.healthpocket.api.dto.UpdateAppointmentStatusRequest
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.Appointment
import com.healthpocket.api.model.AppointmentStatus
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.AppointmentRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(MockKExtension::class)
class AppointmentServiceTest {

    @MockK
    private lateinit var appointmentRepository: AppointmentRepository

    private lateinit var appointmentService: AppointmentService

    private val userId = UUID.randomUUID()
    private val appointmentId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        appointmentService = AppointmentService(appointmentRepository)
    }

    @Test
    fun `getAllAppointments should return all user appointments`() {
        // Given
        val appointments = listOf(
            createAppointment("Appointment 1"),
            createAppointment("Appointment 2")
        )

        every { appointmentRepository.findByUserId(userId) } returns appointments

        // When
        val result = appointmentService.getAllAppointments(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Appointment 1", result[0].title)
        assertEquals("Appointment 2", result[1].title)
    }

    @Test
    fun `getUpcomingAppointments should return only upcoming appointments`() {
        // Given
        val upcomingAppointment = createAppointment("Upcoming")
        every { appointmentRepository.findUpcomingByUserId(userId, any()) } returns listOf(upcomingAppointment)

        // When
        val result = appointmentService.getUpcomingAppointments(userId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Upcoming", result[0].title)
    }

    @Test
    fun `getAppointmentById should return appointment when found`() {
        // Given
        val appointment = createAppointment("Test Appointment")

        every { appointmentRepository.findById(appointmentId) } returns Optional.of(appointment)

        // When
        val result = appointmentService.getAppointmentById(userId, appointmentId)

        // Then
        assertNotNull(result)
        assertEquals("Test Appointment", result.title)
    }

    @Test
    fun `getAppointmentById should throw exception when not found`() {
        // Given
        every { appointmentRepository.findById(appointmentId) } returns Optional.empty()

        // When/Then
        assertThrows<ResourceNotFoundException> {
            appointmentService.getAppointmentById(userId, appointmentId)
        }
    }

    @Test
    fun `getAppointmentById should throw exception when appointment belongs to different user`() {
        // Given
        val differentUserId = UUID.randomUUID()
        val appointment = Appointment(
            id = appointmentId,
            userId = differentUserId,
            title = "Test Appointment",
            appointmentDate = OffsetDateTime.now(),
            syncStatus = SyncStatus.SYNCED
        )

        every { appointmentRepository.findById(appointmentId) } returns Optional.of(appointment)

        // When/Then
        assertThrows<ResourceNotFoundException> {
            appointmentService.getAppointmentById(userId, appointmentId)
        }
    }

    @Test
    fun `createAppointment should save and return appointment`() {
        // Given
        val now = OffsetDateTime.now()
        val request = AppointmentRequest(
            title = "New Appointment",
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = now,
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = com.healthpocket.api.model.AppointmentStatus.SCHEDULED,
            notes = "Notes"
        )

        val savedAppointment = Appointment(
            id = appointmentId,
            userId = userId,
            title = request.title,
            description = request.description,
            doctorName = request.doctorName,
            location = request.location,
            appointmentDate = request.appointmentDate,
            durationMinutes = request.durationMinutes,
            reminderMinutesBefore = request.reminderMinutesBefore,
            reminderEnabled = request.reminderEnabled,
            status = request.status,
            notes = request.notes,
            syncStatus = SyncStatus.SYNCED
        )

        every { appointmentRepository.findByLocalId(any()) } returns null
        every { appointmentRepository.save(any()) } returns savedAppointment

        // When
        val result = appointmentService.createAppointment(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("New Appointment", result.title)
        assertEquals(SyncStatus.SYNCED, result.syncStatus)

        verify { appointmentRepository.save(any()) }
    }

    @Test
    fun `updateAppointment should update and return appointment`() {
        // Given
        val existingAppointment = createAppointment("Existing")
        val request = AppointmentRequest(
            title = "Updated Appointment",
            description = "Updated Description",
            doctorName = "Dr. Jones",
            location = "Clinic",
            appointmentDate = OffsetDateTime.now(),
            durationMinutes = 60,
            reminderMinutesBefore = 30,
            reminderEnabled = false,
            status = AppointmentStatus.COMPLETED,
            notes = "Updated Notes"
        )

        every { appointmentRepository.findById(appointmentId) } returns Optional.of(existingAppointment)
        every { appointmentRepository.save(any()) } returns existingAppointment

        // When
        val result = appointmentService.updateAppointment(userId, appointmentId, request)

        // Then
        assertNotNull(result)
        verify { appointmentRepository.save(any()) }
    }

    @Test
    fun `updateAppointmentStatus should update status`() {
        // Given
        val appointment = createAppointment("Test")
        val request = UpdateAppointmentStatusRequest(status = AppointmentStatus.COMPLETED)

        every { appointmentRepository.findById(appointmentId) } returns Optional.of(appointment)
        every { appointmentRepository.save(any()) } returns appointment

        // When
        val result = appointmentService.updateAppointmentStatus(userId, appointmentId, request)

        // Then
        assertNotNull(result)
        verify { appointmentRepository.save(any()) }
    }

    @Test
    fun `deleteAppointment should delete appointment when found`() {
        // Given
        val appointment = createAppointment("Test")

        every { appointmentRepository.findById(appointmentId) } returns Optional.of(appointment)
        every { appointmentRepository.delete(any()) } returns Unit

        // When
        appointmentService.deleteAppointment(userId, appointmentId)

        // Then
        verify { appointmentRepository.delete(appointment) }
    }

    private fun createAppointment(title: String): Appointment {
        return Appointment(
            id = appointmentId,
            userId = userId,
            title = title,
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = OffsetDateTime.now(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = AppointmentStatus.SCHEDULED,
            notes = "Notes",
            syncStatus = SyncStatus.SYNCED,
            localId = null
        )
    }
}
