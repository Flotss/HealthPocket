package com.healthpocket.ui.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.MedicationEntity
import java.time.format.DateTimeFormatter

/**
 * Screen displaying medication details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: MedicationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.medication_detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            uiState.medication?.let { 
                                onNavigateToEdit(it.id) 
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            uiState.medication != null -> {
                MedicationDetailContent(
                    medication = uiState.medication!!,
                    onToggleStatus = { viewModel.toggleMedicationStatus() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_medication)) },
            text = { 
                Text(
                    stringResource(
                        R.string.delete_medication_confirm, 
                        uiState.medication?.name ?: ""
                    )
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedication()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MedicationDetailContent(
    medication: MedicationEntity,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with color indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(medication.color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (medication.isActive) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = if (medication.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (medication.isActive) 
                            stringResource(R.string.active) 
                        else 
                            stringResource(R.string.inactive),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (medication.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        HorizontalDivider()

        // Dosage information
        DetailSection(
            title = stringResource(R.string.dosage_information),
            content = {
                DetailItem(
                    icon = Icons.Filled.LocalPharmacy,
                    label = stringResource(R.string.dosage),
                    value = medication.dosage
                )
                DetailItem(
                    icon = Icons.Filled.Repeat,
                    label = stringResource(R.string.frequency),
                    value = medication.frequency
                )
            }
        )

        HorizontalDivider()

        // Schedule
        DetailSection(
            title = stringResource(R.string.schedule),
            content = {
                DetailItem(
                    icon = Icons.Filled.Schedule,
                    label = stringResource(R.string.schedule_time),
                    value = medication.scheduleTimes.replace(",", ", ")
                )
                DetailItem(
                    icon = Icons.Filled.CalendarToday,
                    label = stringResource(R.string.start_date),
                    value = medication.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
                if (medication.endDate != null) {
                    DetailItem(
                        icon = Icons.Filled.Event,
                        label = stringResource(R.string.end_date),
                        value = medication.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
                }
            }
        )

        HorizontalDivider()

        // Reminders
        DetailSection(
            title = stringResource(R.string.reminders),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.enable_reminders),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text = if (medication.reminderEnabled) 
                            stringResource(R.string.enabled) 
                        else 
                            stringResource(R.string.disabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        // Notes
        if (!medication.notes.isNullOrBlank()) {
            HorizontalDivider()
            DetailSection(
                title = stringResource(R.string.notes),
                content = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = medication.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            )
        }

        HorizontalDivider()

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onToggleStatus,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (medication.isActive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (medication.isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (medication.isActive) 
                        stringResource(R.string.deactivate) 
                    else 
                        stringResource(R.string.activate)
                )
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

