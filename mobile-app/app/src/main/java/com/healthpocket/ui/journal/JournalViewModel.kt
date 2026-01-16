package com.healthpocket.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.repository.HealthLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class JournalUiState(
    val selectedMood: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val healthLogRepository: HealthLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    val recentLogs: Flow<List<HealthLogEntity>> = healthLogRepository.getRecentHealthLogs(7)

    fun setMood(mood: Int) {
        _uiState.value = _uiState.value.copy(selectedMood = mood)
    }

    fun saveTodayLog() {
        val mood = _uiState.value.selectedMood ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                healthLogRepository.createOrUpdateHealthLog(
                    date = LocalDate.now(),
                    mood = mood
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMood = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun saveDetailedLog(
        mood: Int?,
        energyLevel: Int?,
        sleepQuality: Int?,
        sleepHours: Float?,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                healthLogRepository.createOrUpdateHealthLog(
                    date = LocalDate.now(),
                    mood = mood,
                    energyLevel = energyLevel,
                    sleepQuality = sleepQuality,
                    sleepHours = sleepHours,
                    notes = notes
                )
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

