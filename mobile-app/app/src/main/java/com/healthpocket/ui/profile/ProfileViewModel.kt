package com.healthpocket.ui.profile

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.R
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.repository.AppointmentRepository
import com.healthpocket.data.repository.AuthRepository
import com.healthpocket.data.repository.HealthLogRepository
import com.healthpocket.data.repository.MedicationRepository
import com.healthpocket.data.repository.VitalMetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val vitalMetrics: List<VitalMetricSummary> = emptyList(),
    val stats: ProfileStats = ProfileStats(),
    val formState: ProfileFormState = ProfileFormState(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val snackbarMessage: ProfileMessage? = null
) {
    val displayName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

data class ProfileStats(
    val medicationsTracked: Int = 0,
    val appointmentsScheduled: Int = 0,
    val journalStreak: Int = 0,
    val vitalEntries: Int = 0
)

data class ProfileFormState(
    val firstName: String = "",
    val lastName: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null,
    @StringRes val emergencyContactPhoneError: Int? = null
)

enum class ProfileFormField {
    FIRST_NAME,
    LAST_NAME,
    BLOOD_TYPE,
    ALLERGIES,
    EMERGENCY_CONTACT_NAME,
    EMERGENCY_CONTACT_PHONE
}

data class ProfileMessage(
    @StringRes val messageRes: Int,
    val isError: Boolean = false
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
    private val vitalMetricRepository: VitalMetricRepository,
    private val medicationRepository: MedicationRepository,
    private val appointmentRepository: AppointmentRepository,
    private val healthLogRepository: HealthLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeVitalMetrics()
        observeMedications()
        observeAppointments()
        observeJournalStreak()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            if (!authRepository.isAuthenticated()) {
                return@launch
            }

            authRepository.getCurrentUser().collect { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(
                        firstName = it.firstName,
                        lastName = it.lastName,
                        email = it.email,
                        bloodType = it.bloodType,
                        allergies = it.allergies?.split(",")?.map { allergy -> allergy.trim() }
                            ?.filter { allergy -> allergy.isNotEmpty() }
                            ?: emptyList(),
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

                updateState { current ->
                    current.copy(
                        vitalMetrics = latestByType,
                        stats = current.stats.copy(vitalEntries = metrics.size)
                    )
                }
            }
        }
    }

    private fun observeMedications() {
        viewModelScope.launch {
            medicationRepository.getAllMedications().collect { medications ->
                updateStats { it.copy(medicationsTracked = medications.size) }
            }
        }
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            appointmentRepository.getUpcomingAppointments().collect { appointments ->
                updateStats { it.copy(appointmentsScheduled = appointments.size) }
            }
        }
    }

    private fun observeJournalStreak() {
        viewModelScope.launch {
            healthLogRepository.getAllHealthLogs().collect { logs ->
                val streak = calculateJournalStreak(logs)
                updateStats { it.copy(journalStreak = streak) }
            }
        }
    }

    private fun calculateJournalStreak(logs: List<HealthLogEntity>): Int {
        if (logs.isEmpty()) return 0
        val logDates = logs.map { it.logDate }.toSet()
        var streak = 0
        var cursor = LocalDate.now()

        while (logDates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }

        return streak
    }

    private fun updateStats(transform: (ProfileStats) -> ProfileStats) {
        updateState { current -> current.copy(stats = transform(current.stats)) }
    }

    private fun updateState(transform: (ProfileUiState) -> ProfileUiState) {
        _uiState.value = transform(_uiState.value)
    }

    fun startEditing() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isEditing = true,
            formState = ProfileFormState(
                firstName = state.firstName,
                lastName = state.lastName,
                bloodType = state.bloodType.orEmpty(),
                allergies = state.allergies.joinToString(", "),
                emergencyContactName = state.emergencyContactName.orEmpty(),
                emergencyContactPhone = state.emergencyContactPhone.orEmpty()
            )
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            isSaving = false,
            formState = ProfileFormState()
        )
    }

    fun onFormValueChange(field: ProfileFormField, value: String) {
        val current = _uiState.value.formState
        val updated = when (field) {
            ProfileFormField.FIRST_NAME -> current.copy(firstName = value, firstNameError = null)
            ProfileFormField.LAST_NAME -> current.copy(lastName = value, lastNameError = null)
            ProfileFormField.BLOOD_TYPE -> current.copy(bloodType = value)
            ProfileFormField.ALLERGIES -> current.copy(allergies = value)
            ProfileFormField.EMERGENCY_CONTACT_NAME -> current.copy(emergencyContactName = value)
            ProfileFormField.EMERGENCY_CONTACT_PHONE -> current.copy(
                emergencyContactPhone = value,
                emergencyContactPhoneError = null
            )
        }

        _uiState.value = _uiState.value.copy(formState = updated)
    }

    fun saveProfileChanges() {
        val state = _uiState.value
        val form = state.formState
        val firstName = form.firstName.trim()
        val lastName = form.lastName.trim()
        val phone = form.emergencyContactPhone.trim()

        var firstNameError: Int? = null
        var lastNameError: Int? = null
        var phoneError: Int? = null

        if (firstName.isEmpty()) {
            firstNameError = R.string.profile_first_name_required
        }

        if (lastName.isEmpty()) {
            lastNameError = R.string.profile_last_name_required
        }

        if (phone.isNotEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            phoneError = R.string.profile_invalid_phone
        }

        if (firstNameError != null || lastNameError != null || phoneError != null) {
            _uiState.value = state.copy(
                formState = form.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emergencyContactPhoneError = phoneError
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val allergies = form.allergies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val bloodType = form.bloodType.trim().takeIf { it.isNotEmpty() }?.uppercase()
            val contactName = form.emergencyContactName.trim().takeIf { it.isNotEmpty() }
            val contactPhone = phone.takeIf { it.isNotEmpty() }

            val result = authRepository.updateProfile(
                firstName = firstName,
                lastName = lastName,
                bloodType = bloodType,
                allergies = allergies,
                emergencyContactName = contactName,
                emergencyContactPhone = contactPhone
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        bloodType = user.bloodType,
                        allergies = user.allergies?.split(",")
                            ?.map { it.trim() }
                            ?.filter { it.isNotEmpty() }
                            ?: emptyList(),
                        emergencyContactName = user.emergencyContactName,
                        emergencyContactPhone = user.emergencyContactPhone,
                        isEditing = false,
                        isSaving = false,
                        formState = ProfileFormState(),
                        snackbarMessage = ProfileMessage(R.string.profile_updated)
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        snackbarMessage = ProfileMessage(
                            messageRes = R.string.profile_update_failed,
                            isError = true
                        )
                    )
                }
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
