package com.healthpocket.ui.appointments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentDetailViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: String =
        checkNotNull(savedStateHandle["appointmentId"]) { "appointmentId is required" }

    val appointment: StateFlow<AppointmentEntity?> =
        appointmentRepository.getAppointmentById(appointmentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    fun markAsCompleted() {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)
        }
    }

    fun deleteAppointment(onDeleted: () -> Unit) {
        viewModelScope.launch {
            appointment.value?.let {
                appointmentRepository.deleteAppointment(it)
                onDeleted()
            }
        }
    }
}
