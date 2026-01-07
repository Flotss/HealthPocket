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
import javax.inject.Inject

data class MedicationDetailUiState(
    val medication: MedicationEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

/**
 * ViewModel for the medication detail screen.
 */
@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicationId: String = checkNotNull(savedStateHandle["medicationId"])

    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState: StateFlow<MedicationDetailUiState> = _uiState.asStateFlow()

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

    fun toggleMedicationStatus() {
        viewModelScope.launch {
            _uiState.value.medication?.let { medication ->
                val updated = medication.copy(isActive = !medication.isActive)
                medicationRepository.updateMedication(updated)
            }
        }
    }

    fun deleteMedication() {
        viewModelScope.launch {
            _uiState.value.medication?.let { medication ->
                medicationRepository.deleteMedication(medication)
                _uiState.value = _uiState.value.copy(isDeleted = true)
            }
        }
    }
}

