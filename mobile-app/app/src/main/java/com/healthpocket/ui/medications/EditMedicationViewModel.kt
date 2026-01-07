package com.healthpocket.ui.medications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class EditMedicationUiState(
    val medication: MedicationEntity? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * ViewModel for the edit medication screen.
 */
@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicationId: String = checkNotNull(savedStateHandle["medicationId"])

    private val _uiState = MutableStateFlow(EditMedicationUiState())
    val uiState: StateFlow<EditMedicationUiState> = _uiState.asStateFlow()

    init {
        loadMedication()
    }

    private fun loadMedication() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                medicationRepository.getMedicationById(medicationId).collect { medication ->
                    _uiState.value = _uiState.value.copy(
                        medication = medication,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun saveMedication(
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        endDate: LocalDate? = null,
        notes: String? = null,
        color: String,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                _uiState.value.medication?.let { medication ->
                    val updated = medication.copy(
                        name = name,
                        dosage = dosage,
                        frequency = frequency,
                        scheduleTimes = scheduleTimes.joinToString(","),
                        endDate = endDate,
                        notes = notes,
                        color = color,
                        reminderEnabled = reminderEnabled
                    )
                    medicationRepository.updateMedication(updated)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }
}

