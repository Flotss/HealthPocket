package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AppointmentRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for appointment data operations.
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val api: HealthPocketApi
) {

    fun getAllAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllAppointments()
    }

    fun getUpcomingAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getUpcomingAppointments(System.currentTimeMillis())
    }

    fun getAppointmentsInRange(startDate: Long, endDate: Long): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsInRange(startDate, endDate)
    }

    fun getAppointmentById(id: String): Flow<AppointmentEntity?> {
        return appointmentDao.getAppointmentById(id)
    }

    suspend fun createAppointment(
        title: String,
        description: String? = null,
        doctorName: String? = null,
        location: String? = null,
        appointmentDate: Long,
        durationMinutes: Int = 30,
        reminderMinutesBefore: Int = 60,
        reminderEnabled: Boolean = true,
        notes: String? = null
    ): AppointmentEntity {
        val appointment = AppointmentEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            doctorName = doctorName,
            location = location,
            appointmentDate = appointmentDate,
            durationMinutes = durationMinutes,
            reminderMinutesBefore = reminderMinutesBefore,
            reminderEnabled = reminderEnabled,
            status = AppointmentStatus.SCHEDULED,
            notes = notes,
            syncStatus = SyncStatus.PENDING
        )

        appointmentDao.insert(appointment)
        trySync(appointment)
        
        return appointment
    }

    suspend fun updateAppointment(appointment: AppointmentEntity) {
        val updated = appointment.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        appointmentDao.update(updated)
        trySync(updated)
    }

    suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus) {
        appointmentDao.updateStatus(id, status, SyncStatus.PENDING)
    }

    suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.delete(appointment)
        appointment.serverId?.let { serverId ->
            try {
                api.deleteAppointment(serverId)
            } catch (e: Exception) {
                // Ignore network errors
            }
        }
    }

    private suspend fun trySync(appointment: AppointmentEntity) {
        try {
            val request = AppointmentRequest(
                title = appointment.title,
                description = appointment.description,
                doctorName = appointment.doctorName,
                location = appointment.location,
                appointmentDate = java.time.Instant.ofEpochMilli(appointment.appointmentDate)
                    .atOffset(java.time.ZoneOffset.UTC).toString(),
                durationMinutes = appointment.durationMinutes,
                reminderMinutesBefore = appointment.reminderMinutesBefore,
                reminderEnabled = appointment.reminderEnabled,
                status = appointment.status.name,
                notes = appointment.notes,
                localId = appointment.id
            )

            val response = if (appointment.serverId != null) {
                api.updateAppointment(appointment.serverId, request)
            } else {
                api.createAppointment(request)
            }

            if (response.isSuccessful) {
                response.body()?.let { serverAppointment ->
                    val synced = appointment.copy(
                        serverId = serverAppointment.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    appointmentDao.update(synced)
                }
            }
        } catch (e: Exception) {
            appointmentDao.updateSyncStatus(appointment.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingAppointments(): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsBySyncStatus(SyncStatus.PENDING)
    }
}

