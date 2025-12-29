package com.healthpocket.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAppointmentUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAppointmentUiState())
    val uiState: StateFlow<AddAppointmentUiState> = _uiState.asStateFlow()

    fun saveAppointment(
        title: String,
        doctorName: String?,
        location: String?,
        description: String?,
        appointmentDate: Long,
        durationMinutes: Int = 30,
        reminderMinutesBefore: Int = 60,
        reminderEnabled: Boolean = true,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                appointmentRepository.createAppointment(
                    title = title,
                    description = description,
                    doctorName = doctorName,
                    location = location,
                    appointmentDate = appointmentDate,
                    durationMinutes = durationMinutes,
                    reminderMinutesBefore = reminderMinutesBefore,
                    reminderEnabled = reminderEnabled,
                    notes = notes
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

