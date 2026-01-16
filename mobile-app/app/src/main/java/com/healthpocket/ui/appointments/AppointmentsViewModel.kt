package com.healthpocket.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    val upcomingAppointments: Flow<List<AppointmentEntity>> =
        appointmentRepository.getUpcomingAppointments()

    val pastAppointments: Flow<List<AppointmentEntity>> =
        appointmentRepository.getPastAppointments()

    fun markAsCompleted(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(
                appointmentId,
                AppointmentStatus.COMPLETED
            )
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointment)
        }
    }
}
