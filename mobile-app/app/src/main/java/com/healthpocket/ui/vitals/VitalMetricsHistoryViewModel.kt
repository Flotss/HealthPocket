package com.healthpocket.ui.vitals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.VitalMetricEntity
import com.healthpocket.data.repository.VitalMetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VitalHistoryUiState(
    val entries: List<VitalMetricEntity> = emptyList()
)

@HiltViewModel
class VitalMetricsHistoryViewModel @Inject constructor(
    private val vitalMetricRepository: VitalMetricRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VitalHistoryUiState())
    val uiState: StateFlow<VitalHistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            vitalMetricRepository.getAllVitalMetrics().collect { metrics ->
                _uiState.value = VitalHistoryUiState(
                    entries = metrics.sortedByDescending { it.measuredAt }
                )
            }
        }
    }
}
