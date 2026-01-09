package com.healthpocket.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.repository.AuthRepository
import com.healthpocket.data.repository.VitalMetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val email: String = "",
    val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val vitalMetrics: List<VitalMetricSummary> = emptyList()
)

data class VitalMetricSummary(
    val type: MetricType,
    val value: Float,
    val secondaryValue: Float? = null,
    val unit: String,
    val measuredAt: Long,
    val notes: String?
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val vitalMetricRepository: VitalMetricRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeVitalMetrics()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            if (!authRepository.isAuthenticated()) {
                return@launch
            }
            
            authRepository.getCurrentUser().collect { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(
                        userName = "${it.firstName} ${it.lastName}",
                        email = it.email,
                        bloodType = it.bloodType,
                        allergies = it.allergies?.split(",")?.filter { a -> a.isNotBlank() } ?: emptyList(),
                        emergencyContactName = it.emergencyContactName,
                        emergencyContactPhone = it.emergencyContactPhone
                    )
                }
            }
        }
    }

    private fun observeVitalMetrics() {
        viewModelScope.launch {
            vitalMetricRepository.getAllVitalMetrics().collect { metrics ->
                val latestByType = metrics
                    .groupBy { it.metricType }
                    .mapNotNull { (_, entries) ->
                        entries.maxByOrNull { it.measuredAt }
                    }
                    .sortedBy { it.metricType.ordinal }
                    .map {
                        VitalMetricSummary(
                            type = it.metricType,
                            value = it.value,
                            secondaryValue = it.secondaryValue,
                            unit = it.unit,
                            measuredAt = it.measuredAt,
                            notes = it.notes
                        )
                    }

                _uiState.value = _uiState.value.copy(vitalMetrics = latestByType)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
