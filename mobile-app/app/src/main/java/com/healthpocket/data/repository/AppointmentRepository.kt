package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AppointmentRequest
import com.healthpocket.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
        return appointmentDao.getAllAppointments().distinctUntilChanged()
    }

    fun getUpcomingAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getUpcomingAppointments(System.currentTimeMillis()).distinctUntilChanged()
    }

    fun getAppointmentsInRange(startDate: Long, endDate: Long): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsInRange(startDate, endDate).distinctUntilChanged()
    }

    fun getAppointmentById(id: String): Flow<AppointmentEntity?> {
        return appointmentDao.getAppointmentById(id).distinctUntilChanged()
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
                appointmentDate = DateTimeUtils.millisToOffsetDateTimeString(appointment.appointmentDate),
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

    suspend fun syncAppointments() {
        try {
            val appointments = api.getAllAppointments().body()
            appointments?.forEach { serverAppointment ->
                val localAppointment = appointmentDao.getAppointmentByIdSync(serverAppointment.id)
                if (localAppointment == null) {
                    val entity = AppointmentEntity(
                        id = UUID.randomUUID().toString(),
                        serverId = serverAppointment.id,
                        title = serverAppointment.title,
                        description = serverAppointment.description,
                        doctorName = serverAppointment.doctorName,
                        location = serverAppointment.location,
                        appointmentDate = DateTimeUtils.offsetDateTimeStringToMillis(serverAppointment.appointmentDate),
                        durationMinutes = serverAppointment.durationMinutes ?: 30,
                        reminderMinutesBefore = serverAppointment.reminderMinutesBefore ?: 60,
                        reminderEnabled = serverAppointment.reminderEnabled ?: true,
                        status = try {
                            AppointmentStatus.valueOf(serverAppointment.status ?: "SCHEDULED")
                        } catch (e: Exception) {
                            AppointmentStatus.SCHEDULED
                        },
                        notes = serverAppointment.notes,
                        syncStatus = SyncStatus.SYNCED
                    )
                    appointmentDao.insert(entity)
                }
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }
}

