package com.healthpocket.ui.medications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.ui.theme.MedicationColors
import java.time.LocalDate

/**
 * Screen for adding a new medication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("08:00") }
    var notes by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var reminderEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_medication)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medication name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.medication_name)) },
                leadingIcon = { Icon(Icons.Filled.Medication, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text(stringResource(R.string.dosage)) },
                placeholder = { Text("e.g., 500mg, 2 tablets") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency
            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text(stringResource(R.string.frequency)) },
                placeholder = { Text("e.g., 3 times per day") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Schedule time
            OutlinedTextField(
                value = scheduleTime,
                onValueChange = { scheduleTime = it },
                label = { Text(stringResource(R.string.schedule_time)) },
                placeholder = { Text("08:00, 14:00, 20:00") },
                leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Color picker
            Text(
                text = stringResource(R.string.color),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationColors.forEachIndexed { index, color ->
                    FilterChip(
                        selected = selectedColorIndex == index,
                        onClick = { selectedColorIndex = index },
                        label = { Text("") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = color,
                            selectedContainerColor = color
                        ),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enable_reminders),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveMedication(
                        name = name,
                        dosage = dosage,
                        frequency = frequency,
                        scheduleTimes = scheduleTime.split(",").map { it.trim() },
                        startDate = LocalDate.now(),
                        notes = notes.ifBlank { null },
                        color = String.format("#%06X", 0xFFFFFF and MedicationColors[selectedColorIndex].hashCode()),
                        reminderEnabled = reminderEnabled
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

