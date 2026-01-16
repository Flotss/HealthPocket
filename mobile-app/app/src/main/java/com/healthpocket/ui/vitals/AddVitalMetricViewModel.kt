package com.healthpocket.ui.vitals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.repository.VitalMetricRepository
import com.healthpocket.util.MetricUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddVitalMetricUiState(
    val metricFields: Map<MetricType, MetricInputFields> = createEmptyMetricFields(),
    val existingMetrics: Map<MetricType, ExistingMetricSummary?> = emptyMap(),
    val dirtyMetrics: Set<MetricType> = emptySet(),
    val fieldErrors: Map<MetricType, AddVitalMetricError> = emptyMap(),
    val generalError: AddVitalMetricError? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val metricOrder: List<MetricType> = MetricType.entries,
    val hasInitialized: Boolean = false
)

data class MetricInputFields(
    val primaryValue: String = "",
    val secondaryValue: String = "",
    val notes: String = ""
)

enum class AddVitalMetricError {
    PRIMARY_VALUE_REQUIRED,
    INVALID_PRIMARY_VALUE,
    INVALID_SECONDARY_VALUE,
    SAVE_FAILED,
    NOTHING_TO_SAVE
}

data class ExistingMetricSummary(
    val value: Float,
    val secondaryValue: Float?,
    val unit: String,
    val measuredAt: Long,
    val notes: String?
)

@HiltViewModel
class AddVitalMetricViewModel @Inject constructor(
    private val vitalMetricRepository: VitalMetricRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVitalMetricUiState())
    val uiState: StateFlow<AddVitalMetricUiState> = _uiState.asStateFlow()

    private var hasPrefilledExistingValues = false

    init {
        observeExistingMetrics()
    }

    fun setInitialType(type: MetricType) {
        _uiState.update { state ->
            if (state.hasInitialized) {
                state
            } else {
                val reordered = listOf(type) + MetricType.entries.filter { it != type }
                state.copy(metricOrder = reordered, hasInitialized = true)
            }
        }
    }

    fun updatePrimaryValue(type: MetricType, value: String) {
        _uiState.update { current ->
            val updatedFields = current.metricFields + (type to (current.metricFields[type]
                ?: MetricInputFields()).copy(primaryValue = value))
            current.copy(
                metricFields = updatedFields,
                fieldErrors = current.fieldErrors - type,
                generalError = null,
                dirtyMetrics = updateDirtyMetrics(type, updatedFields, current.dirtyMetrics)
            )
        }
    }

    fun updateSecondaryValue(type: MetricType, value: String) {
        _uiState.update { current ->
            val updatedFields = current.metricFields + (type to (current.metricFields[type]
                ?: MetricInputFields()).copy(secondaryValue = value))
            current.copy(
                metricFields = updatedFields,
                fieldErrors = current.fieldErrors - type,
                generalError = null,
                dirtyMetrics = updateDirtyMetrics(type, updatedFields, current.dirtyMetrics)
            )
        }
    }

    fun updateMetricNotes(type: MetricType, value: String) {
        _uiState.update { currentState ->
            val updatedFields =
                currentState.metricFields + (type to (currentState.metricFields[type]
                    ?: MetricInputFields()).copy(notes = value))
            currentState.copy(
                metricFields = updatedFields,
                fieldErrors = currentState.fieldErrors - type,
                generalError = null,
                dirtyMetrics = updateDirtyMetrics(type, updatedFields, currentState.dirtyMetrics)
            )
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun saveMetric() {
        val currentState = _uiState.value
        val pendingMetrics = mutableListOf<PendingMetric>()
        val fieldErrors = mutableMapOf<MetricType, AddVitalMetricError>()
        val dirtyTypes = currentState.dirtyMetrics

        if (dirtyTypes.isEmpty()) {
            _uiState.update { it.copy(generalError = AddVitalMetricError.NOTHING_TO_SAVE) }
            return
        }

        dirtyTypes.forEach { type ->
            val fields = currentState.metricFields[type] ?: MetricInputFields()
            if (fields.primaryValue.isBlank()) {
                fieldErrors[type] = AddVitalMetricError.PRIMARY_VALUE_REQUIRED
                return@forEach
            }

            val primary = fields.primaryValue.toFloatOrNull()
            if (primary == null) {
                fieldErrors[type] = AddVitalMetricError.INVALID_PRIMARY_VALUE
                return@forEach
            }

            var secondary: Float? = null
            if (type == MetricType.BLOOD_PRESSURE) {
                if (fields.secondaryValue.isBlank()) {
                    fieldErrors[type] = AddVitalMetricError.INVALID_SECONDARY_VALUE
                    return@forEach
                }
                secondary = fields.secondaryValue.toFloatOrNull()
                if (secondary == null) {
                    fieldErrors[type] = AddVitalMetricError.INVALID_SECONDARY_VALUE
                    return@forEach
                }
            }

            pendingMetrics.add(
                PendingMetric(
                    type = type,
                    value = primary,
                    secondaryValue = secondary,
                    notes = fields.notes.ifBlank { null }
                )
            )
        }

        if (fieldErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, generalError = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    fieldErrors = emptyMap(),
                    generalError = null
                )
            }
            try {
                pendingMetrics.forEach { metric ->
                    vitalMetricRepository.createVitalMetric(
                        metricType = metric.type,
                        value = metric.value,
                        secondaryValue = metric.secondaryValue,
                        unit = MetricUtils.getUnitForType(metric.type),
                        notes = metric.notes
                    )
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        metricFields = createEmptyMetricFields(),
                        dirtyMetrics = emptySet()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        generalError = AddVitalMetricError.SAVE_FAILED
                    )
                }
            }
        }
    }

    private fun observeExistingMetrics() {
        viewModelScope.launch {
            vitalMetricRepository.getAllVitalMetrics().collect { metrics ->
                val latestByType = MetricType.entries.associateWith { type ->
                    metrics.filter { it.metricType == type }
                        .maxByOrNull { it.measuredAt }
                        ?.let { entity ->
                            ExistingMetricSummary(
                                value = entity.value,
                                secondaryValue = entity.secondaryValue,
                                unit = entity.unit,
                                measuredAt = entity.measuredAt,
                                notes = entity.notes
                            )
                        }
                }

                _uiState.update { current ->
                    var updatedState = current.copy(existingMetrics = latestByType)
                    if (!hasPrefilledExistingValues) {
                        val prefilledFields = current.metricFields.toMutableMap()
                        latestByType.forEach { (type, summary) ->
                            summary?.let {
                                prefilledFields[type] = MetricInputFields(
                                    primaryValue = it.value.toCleanString(),
                                    secondaryValue = it.secondaryValue?.toCleanString() ?: "",
                                    notes = it.notes.orEmpty()
                                )
                            }
                        }
                        updatedState = updatedState.copy(metricFields = prefilledFields)
                        hasPrefilledExistingValues = true
                    }
                    updatedState
                }
            }
        }
    }

    private fun updateDirtyMetrics(
        type: MetricType,
        fields: Map<MetricType, MetricInputFields>,
        currentDirty: Set<MetricType>
    ): Set<MetricType> {
        val field = fields[type] ?: return currentDirty
        val shouldBeDirty =
            field.primaryValue.isNotBlank() || field.secondaryValue.isNotBlank() || field.notes.isNotBlank()
        return if (shouldBeDirty) {
            currentDirty + type
        } else {
            currentDirty - type
        }
    }

    private data class PendingMetric(
        val type: MetricType,
        val value: Float,
        val secondaryValue: Float?,
        val notes: String?
    )

}

private fun createEmptyMetricFields(): Map<MetricType, MetricInputFields> {
    return MetricType.entries.associateWith { MetricInputFields() }
}

private fun Float.toCleanString(): String {
    return if (this % 1f == 0f) {
        toInt().toString()
    } else {
        String.format(java.util.Locale.getDefault(), "%.1f", this)
    }
}
