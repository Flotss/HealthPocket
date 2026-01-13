package com.healthpocket.data.repository

import android.util.Log
import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AppointmentRequest
import com.healthpocket.data.remote.dto.AppointmentResponse
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

    fun getPastAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getPastAppointments(System.currentTimeMillis()).distinctUntilChanged()
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
        
        // Sync status change to server
        val appointment = appointmentDao.getAppointmentByIdSync(id)
        appointment?.let { trySync(it) }
    }

    suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.delete(appointment)
        appointment.serverId?.let { serverId ->
            try {
                Log.d("AppointmentRepository", "Syncing deletion for appointment ${appointment.id}")
                api.deleteAppointment(serverId)
                Log.d("AppointmentRepository", "Deletion synced for appointment ${appointment.id}")
            } catch (e: Exception) {
                Log.e("AppointmentRepository", "Deletion sync failed for appointment ${appointment.id}", e)
                // Ignore network errors
            }
        }
    }

    private suspend fun trySync(appointment: AppointmentEntity) {
        Log.d("AppointmentRepository", "Starting sync for appointment ${appointment.id}")
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
                Log.d("AppointmentRepository", "Sync successful for appointment ${appointment.id}")
                response.body()?.let { serverAppointment ->
                    val synced = appointment.copy(
                        serverId = serverAppointment.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                    appointmentDao.update(synced)
                }
            } else {
                Log.e("AppointmentRepository", "Sync failed for appointment ${appointment.id}: ${response.code()}")
                appointmentDao.updateSyncStatus(appointment.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Sync exception for appointment ${appointment.id}", e)
            appointmentDao.updateSyncStatus(appointment.id, SyncStatus.ERROR)
        }
    }

    suspend fun getPendingAppointments(): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsBySyncStatus(SyncStatus.PENDING)
    }

    suspend fun syncAll() {
        Log.d("AppointmentRepository", "Starting bidirectional sync of appointments")
        try {
            val serverAppointments = api.getAllAppointments().body() ?: emptyList()
            val localAppointments = appointmentDao.getAllAppointmentsAsync()
            
            mergeAndSync(serverAppointments, localAppointments)
            
            Log.d("AppointmentRepository", "Bidirectional sync completed successfully")
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Bidirectional sync failed", e)
        }
    }
    
    @Deprecated("Use syncAll() for bidirectional sync", ReplaceWith("syncAll()"))
    suspend fun syncAppointments() {
        syncAll()
    }
    
    private suspend fun mergeAndSync(
        serverAppointments: List<AppointmentResponse>,
        localAppointments: List<AppointmentEntity>
    ) {
        val serverById = serverAppointments.associateBy { it.id }
        val localByServerId = localAppointments
            .filter { it.serverId != null }
            .associateBy { it.serverId!! }
        
        // Process server appointments
        serverAppointments.forEach { serverAppointment ->
            val localMatch = localByServerId[serverAppointment.id]
            
            when {
                localMatch == null -> {
                    handleNewServerAppointment(serverAppointment)
                }
                isServerNewer(serverAppointment.updatedAt, localMatch.updatedAt) -> {
                    handleServerNewerAppointment(serverAppointment, localMatch)
                }
                isLocalNewer(serverAppointment.updatedAt, localMatch.updatedAt) -> {
                    handleLocalNewerAppointment(serverAppointment, localMatch)
                }
                // If timestamps equal, no action needed
            }
        }
        
        // Process unsynchronized local appointments
        val unsyncedLocalAppointments = localAppointments.filter { it.serverId == null }
        unsyncedLocalAppointments.forEach { localAppointment ->
            pushLocalAppointmentToServer(localAppointment)
        }
    }
    
    private suspend fun handleNewServerAppointment(serverAppointment: AppointmentResponse) {
        Log.d("AppointmentRepository", "Adding new appointment from server: ${serverAppointment.id}")
        val entity = serverAppointment.toEntity()
        appointmentDao.insert(entity)
    }
    
    private suspend fun handleServerNewerAppointment(
        serverAppointment: AppointmentResponse,
        localAppointment: AppointmentEntity
    ) {
        Log.d("AppointmentRepository", "Updating local appointment from server: ${serverAppointment.id}")
        val updatedEntity = serverAppointment.toEntity(localId = localAppointment.id)
        appointmentDao.update(updatedEntity)
    }
    
    private suspend fun handleLocalNewerAppointment(
        serverAppointment: AppointmentResponse,
        localAppointment: AppointmentEntity
    ) {
        Log.d("AppointmentRepository", "Updating server with local appointment: ${localAppointment.id}")
        try {
            val request = localAppointment.toRequest()
            val response = api.updateAppointment(serverAppointment.id, request)
            
            if (response.isSuccessful) {
                appointmentDao.updateSyncStatus(localAppointment.id, SyncStatus.SYNCED)
            } else {
                Log.e("AppointmentRepository", "Failed to update server appointment: ${response.code()}")
                appointmentDao.updateSyncStatus(localAppointment.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("AppointmentRepository", "Network error updating appointment to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }
    
    private suspend fun pushLocalAppointmentToServer(localAppointment: AppointmentEntity) {
        Log.d("AppointmentRepository", "Pushing local appointment to server: ${localAppointment.id}")
        try {
            val request = localAppointment.toRequest()
            val response = api.createAppointment(request)
            
            if (response.isSuccessful) {
                response.body()?.let { serverAppointment ->
                    appointmentDao.updateServerIdAndStatus(
                        localId = localAppointment.id,
                        serverId = serverAppointment.id,
                        status = SyncStatus.SYNCED
                    )
                }
            } else {
                Log.e("AppointmentRepository", "Failed to create appointment on server: ${response.code()}")
                appointmentDao.updateSyncStatus(localAppointment.id, SyncStatus.ERROR)
            }
        } catch (e: Exception) {
            Log.w("AppointmentRepository", "Network error pushing appointment to server: ${e.message}")
            // Keep PENDING status for retry when network returns
        }
    }
    
    private fun isServerNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return serverMillis > localUpdatedAt
    }
    
    private fun isLocalNewer(serverUpdatedAt: String, localUpdatedAt: Long): Boolean {
        val serverMillis = DateTimeUtils.offsetDateTimeStringToMillis(serverUpdatedAt)
        return localUpdatedAt > serverMillis
    }
    
    private fun AppointmentResponse.toEntity(
        localId: String = UUID.randomUUID().toString()
    ): AppointmentEntity {
        return AppointmentEntity(
            id = localId,
            serverId = this.id,
            title = this.title,
            description = this.description,
            doctorName = this.doctorName,
            location = this.location,
            appointmentDate = DateTimeUtils.offsetDateTimeStringToMillis(this.appointmentDate),
            durationMinutes = this.durationMinutes,
            reminderMinutesBefore = this.reminderMinutesBefore,
            reminderEnabled = this.reminderEnabled,
            status = try {
                AppointmentStatus.valueOf(this.status)
            } catch (e: Exception) {
                AppointmentStatus.SCHEDULED
            },
            notes = this.notes,
            syncStatus = SyncStatus.SYNCED,
            updatedAt = DateTimeUtils.offsetDateTimeStringToMillis(this.updatedAt)
        )
    }
    
    private fun AppointmentEntity.toRequest(): AppointmentRequest {
        return AppointmentRequest(
            title = this.title,
            description = this.description,
            doctorName = this.doctorName,
            location = this.location,
            appointmentDate = DateTimeUtils.millisToOffsetDateTimeString(this.appointmentDate),
            durationMinutes = this.durationMinutes,
            reminderMinutesBefore = this.reminderMinutesBefore,
            reminderEnabled = this.reminderEnabled,
            status = this.status.name,
            notes = this.notes,
            localId = this.id
        )
    }
}
