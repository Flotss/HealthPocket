package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.Appointment
import com.healthpocket.api.model.SyncStatus
import com.healthpocket.api.repository.AppointmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository
) {

    fun getAllAppointments(userId: UUID): List<AppointmentResponse> {
        return appointmentRepository.findByUserId(userId)
            .map { AppointmentResponse.fromEntity(it) }
    }

    fun getUpcomingAppointments(userId: UUID): List<AppointmentResponse> {
        return appointmentRepository.findUpcomingByUserId(userId, OffsetDateTime.now())
            .map { AppointmentResponse.fromEntity(it) }
    }

    fun getAppointmentsInRange(userId: UUID, startDate: OffsetDateTime, endDate: OffsetDateTime): List<AppointmentResponse> {
        return appointmentRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .map { AppointmentResponse.fromEntity(it) }
    }

    fun getAppointmentById(userId: UUID, appointmentId: UUID): AppointmentResponse {
        val appointment = findAppointmentByIdAndUser(userId, appointmentId)
        return AppointmentResponse.fromEntity(appointment)
    }

    @Transactional
    fun createAppointment(userId: UUID, request: AppointmentRequest): AppointmentResponse {
        // Check if appointment with same localId already exists
        request.localId?.let { localId ->
            appointmentRepository.findByLocalId(localId)?.let {
                return updateAppointmentByLocalId(userId, localId, request)
            }
        }

        val appointment = Appointment(
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
            syncStatus = SyncStatus.SYNCED,
            localId = request.localId
        )

        val savedAppointment = appointmentRepository.save(appointment)
        return AppointmentResponse.fromEntity(savedAppointment)
    }

    @Transactional
    fun updateAppointment(userId: UUID, appointmentId: UUID, request: AppointmentRequest): AppointmentResponse {
        val appointment = findAppointmentByIdAndUser(userId, appointmentId)

        appointment.title = request.title
        appointment.description = request.description
        appointment.doctorName = request.doctorName
        appointment.location = request.location
        appointment.appointmentDate = request.appointmentDate
        appointment.durationMinutes = request.durationMinutes
        appointment.reminderMinutesBefore = request.reminderMinutesBefore
        appointment.reminderEnabled = request.reminderEnabled
        appointment.status = request.status
        appointment.notes = request.notes
        appointment.syncStatus = SyncStatus.SYNCED

        val savedAppointment = appointmentRepository.save(appointment)
        return AppointmentResponse.fromEntity(savedAppointment)
    }

    private fun updateAppointmentByLocalId(userId: UUID, localId: String, request: AppointmentRequest): AppointmentResponse {
        val appointment = appointmentRepository.findByLocalId(localId)
            ?: throw ResourceNotFoundException("Appointment not found with localId: $localId")

        if (appointment.userId != userId) {
            throw ResourceNotFoundException("Appointment not found")
        }

        appointment.title = request.title
        appointment.description = request.description
        appointment.doctorName = request.doctorName
        appointment.location = request.location
        appointment.appointmentDate = request.appointmentDate
        appointment.durationMinutes = request.durationMinutes
        appointment.reminderMinutesBefore = request.reminderMinutesBefore
        appointment.reminderEnabled = request.reminderEnabled
        appointment.status = request.status
        appointment.notes = request.notes
        appointment.syncStatus = SyncStatus.SYNCED

        val savedAppointment = appointmentRepository.save(appointment)
        return AppointmentResponse.fromEntity(savedAppointment)
    }

    @Transactional
    fun updateAppointmentStatus(userId: UUID, appointmentId: UUID, request: UpdateAppointmentStatusRequest): AppointmentResponse {
        val appointment = findAppointmentByIdAndUser(userId, appointmentId)

        appointment.status = request.status
        appointment.syncStatus = SyncStatus.SYNCED

        val savedAppointment = appointmentRepository.save(appointment)
        return AppointmentResponse.fromEntity(savedAppointment)
    }

    @Transactional
    fun deleteAppointment(userId: UUID, appointmentId: UUID) {
        val appointment = findAppointmentByIdAndUser(userId, appointmentId)
        appointmentRepository.delete(appointment)
    }

    private fun findAppointmentByIdAndUser(userId: UUID, appointmentId: UUID): Appointment {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found with id: $appointmentId") }

        if (appointment.userId != userId) {
            throw ResourceNotFoundException("Appointment not found")
        }

        return appointment
    }
}

