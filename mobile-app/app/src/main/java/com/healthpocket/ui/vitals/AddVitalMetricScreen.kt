package com.healthpocket.ui.vitals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.util.MetricUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVitalMetricScreen(
    initialType: MetricType,
    onNavigateBack: () -> Unit,
    viewModel: AddVitalMetricViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialType) {
        viewModel.setInitialType(initialType)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.consumeSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_vital_metric)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_vital_metric_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.metricOrder.forEach { type ->
                val fields = uiState.metricFields[type] ?: MetricInputFields()
                val existing = uiState.existingMetrics[type]
                MetricInputSection(
                    type = type,
                    fields = fields,
                    existingMetric = existing,
                    error = uiState.fieldErrors[type],
                    onPrimaryValueChange = { viewModel.updatePrimaryValue(type, it) },
                    onSecondaryValueChange = { viewModel.updateSecondaryValue(type, it) },
                    onNotesChange = { viewModel.updateMetricNotes(type, it) }
                )
            }

            uiState.generalError?.let { error ->
                val errorText = when (error) {
                    AddVitalMetricError.NOTHING_TO_SAVE -> stringResource(R.string.metric_no_values)
                    AddVitalMetricError.SAVE_FAILED -> stringResource(R.string.metric_save_failed)
                    else -> null
                }
                if (errorText != null) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = { viewModel.saveMetric() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save_metric))
                }
            }
        }
    }
}

@Composable
private fun MetricInputSection(
    type: MetricType,
    fields: MetricInputFields,
    existingMetric: ExistingMetricSummary?,
    error: AddVitalMetricError?,
    onPrimaryValueChange: (String) -> Unit,
    onSecondaryValueChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    val unit = MetricUtils.getUnitForType(type)
    val typeLabel = stringResource(metricTypeLabelRes(type))
    val primaryError = error == AddVitalMetricError.INVALID_PRIMARY_VALUE || error == AddVitalMetricError.PRIMARY_VALUE_REQUIRED
    val secondaryError = error == AddVitalMetricError.INVALID_SECONDARY_VALUE

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            existingMetric?.let { summary ->
                Text(
                    text = stringResource(
                        R.string.latest_measurement,
                        formatMeasurement(summary.value, summary.secondaryValue, summary.unit),
                        formatMetricTimestamp(summary.measuredAt)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                summary.notes?.takeIf { it.isNotBlank() }?.let { existingNotes ->
                    Text(
                        text = stringResource(R.string.metric_notes_label, existingNotes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedTextField(
                value = fields.primaryValue,
                onValueChange = onPrimaryValueChange,
                label = { Text(stringResource(R.string.metric_value_with_unit, unit)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = primaryError
            )
            if (primaryError) {
                Text(
                    text = stringResource(
                        if (error == AddVitalMetricError.PRIMARY_VALUE_REQUIRED) R.string.metric_value_required else R.string.metric_value_invalid
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (type == MetricType.BLOOD_PRESSURE) {
                OutlinedTextField(
                    value = fields.secondaryValue,
                    onValueChange = onSecondaryValueChange,
                    label = { Text(stringResource(R.string.metric_secondary_value_with_unit, unit)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = secondaryError
                )
                if (secondaryError) {
                    Text(
                        text = stringResource(R.string.metric_secondary_value_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = fields.notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun metricTypeLabelRes(type: MetricType): Int {
    return when (type) {
        MetricType.WEIGHT -> R.string.metric_type_weight
        MetricType.BLOOD_PRESSURE -> R.string.metric_type_blood_pressure
        MetricType.BLOOD_GLUCOSE -> R.string.metric_type_blood_glucose
        MetricType.HEART_RATE -> R.string.metric_type_heart_rate
        MetricType.TEMPERATURE -> R.string.metric_type_temperature
    }
}

private fun formatMeasurement(value: Float, secondary: Float?, unit: String): String {
    val formattedPrimary = value.toDisplayString()
    return if (secondary != null) {
        "$formattedPrimary / ${secondary.toDisplayString()} $unit"
    } else {
        "$formattedPrimary $unit"
    }
}

private fun formatMetricTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun Float.toDisplayString(): String {
    return if (this % 1f == 0f) {
        toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", this)
    }
}
