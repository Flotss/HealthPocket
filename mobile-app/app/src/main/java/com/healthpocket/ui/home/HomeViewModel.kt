package com.healthpocket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.MedicationIntakeEntity
import com.healthpocket.data.repository.AppointmentRepository
import com.healthpocket.data.repository.AuthRepository
import com.healthpocket.data.repository.HealthLogRepository
import com.healthpocket.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI state for the home screen.
 */
data class HomeUiState(
    val userName: String = "",
    val pendingMedicationsCount: Int = 0,
    val upcomingAppointmentsCount: Int = 0,
    val todayLogExists: Boolean = false,
    val todaysPendingIntakes: List<MedicationIntakeEntity> = emptyList(),
    val medicationNames: Map<String, String> = emptyMap()
)

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val medicationRepository: MedicationRepository,
    private val appointmentRepository: AppointmentRepository,
    private val healthLogRepository: HealthLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // Load user name
            authRepository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(
                    userName = user?.let { "${it.firstName} ${it.lastName}" } ?: ""
                )
            }
        }

        viewModelScope.launch {
            // Load today's pending intakes
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            medicationRepository.getTodaysPendingIntakes(startOfDay, endOfDay).collect { intakes ->
                _uiState.value = _uiState.value.copy(
                    todaysPendingIntakes = intakes,
                    pendingMedicationsCount = intakes.size
                )
            }
        }

        viewModelScope.launch {
            // Load medication names for display
            medicationRepository.getAllMedications().collect { medications ->
                val names = medications.associate { it.id to it.name }
                _uiState.value = _uiState.value.copy(medicationNames = names)
            }
        }

        viewModelScope.launch {
            // Load upcoming appointments count
            appointmentRepository.getUpcomingAppointments().collect { appointments ->
                _uiState.value = _uiState.value.copy(
                    upcomingAppointmentsCount = appointments.size
                )
            }
        }

        viewModelScope.launch {
            // Check if today's log exists
            healthLogRepository.getHealthLogByDate(LocalDate.now()).collect { log ->
                _uiState.value = _uiState.value.copy(
                    todayLogExists = log != null
                )
            }
        }
    }

    fun markIntakeAsTaken(intake: MedicationIntakeEntity) {
        viewModelScope.launch {
            medicationRepository.markIntakeAsTaken(intake)
        }
    }

    fun markIntakeAsSkipped(intake: MedicationIntakeEntity) {
        viewModelScope.launch {
            medicationRepository.markIntakeAsSkipped(intake)
        }
    }
}

