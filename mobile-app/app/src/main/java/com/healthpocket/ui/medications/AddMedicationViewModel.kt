package com.healthpocket.ui.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.repository.MedicationRepository
import com.healthpocket.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddMedicationUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun saveMedication(
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        notes: String? = null,
        color: String = AppColors.MEDICATION_DEFAULT,
        reminderEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                medicationRepository.createMedication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    scheduleTimes = scheduleTimes,
                    startDate = startDate,
                    endDate = endDate,
                    notes = notes,
                    color = color,
                    reminderEnabled = reminderEnabled
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

