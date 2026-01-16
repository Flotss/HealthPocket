package com.healthpocket.ui.vitals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.VitalMetricEntity
import com.healthpocket.util.MetricUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalMetricsHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: VitalMetricsHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vitals_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.entries.isEmpty()) {
            HistoryEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(uiState.entries) { entry ->
                    VitalHistoryCard(entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.vitals_history),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.no_vital_history),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun VitalHistoryCard(entry: VitalMetricEntity) {
    val unit = entry.unit.ifBlank { MetricUtils.getUnitForType(entry.metricType) }
    val measurement = if (entry.secondaryValue != null) {
        "${formatValue(entry.value)} / ${formatValue(entry.secondaryValue)} $unit"
    } else {
        "${formatValue(entry.value)} $unit"
    }
    val timestamp = formatTimestamp(entry.measuredAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(metricTypeLabelRes(entry.metricType)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = measurement,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entry.notes?.takeIf { it.isNotBlank() }?.let { noteText ->
                Text(
                    text = stringResource(R.string.metric_notes_label, noteText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timeMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

private fun formatValue(value: Float): String {
    return if (value % 1f == 0f) value.toInt().toString() else String.format(
        Locale.getDefault(),
        "%.1f",
        value
    )
}

private fun metricTypeLabelRes(type: com.healthpocket.data.local.entity.MetricType): Int {
    return when (type) {
        com.healthpocket.data.local.entity.MetricType.WEIGHT -> R.string.metric_type_weight
        com.healthpocket.data.local.entity.MetricType.BLOOD_PRESSURE -> R.string.metric_type_blood_pressure
        com.healthpocket.data.local.entity.MetricType.BLOOD_GLUCOSE -> R.string.metric_type_blood_glucose
        com.healthpocket.data.local.entity.MetricType.HEART_RATE -> R.string.metric_type_heart_rate
        com.healthpocket.data.local.entity.MetricType.TEMPERATURE -> R.string.metric_type_temperature
    }
}
