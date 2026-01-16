package com.healthpocket.ui.medications

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.model.MedicationReminder
import com.healthpocket.util.ColorUtils
import com.healthpocket.util.remindersToScheduleTimes
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
    var reminders by remember { mutableStateOf<List<MedicationReminder>>(emptyList()) }
    var notes by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && uiState.error != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_medication)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        MedicationForm(
            name = name,
            onNameChange = { name = it },
            dosage = dosage,
            onDosageChange = { dosage = it },
            frequency = frequency,
            onFrequencyChange = { frequency = it },
            reminders = reminders,
            onRemindersChange = { reminders = it },
            notes = notes,
            onNotesChange = { notes = it },
            selectedColorIndex = selectedColorIndex,
            onColorIndexChange = { selectedColorIndex = it },
            isLoading = uiState.isLoading,
            onSave = {
                val scheduleTimes = remindersToScheduleTimes(reminders)

                viewModel.saveMedication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    scheduleTimes = scheduleTimes,
                    startDate = LocalDate.now(),
                    notes = notes.ifBlank { null },
                    color = ColorUtils.getMedicationColorHex(selectedColorIndex),
                    reminderEnabled = reminders.isNotEmpty() && reminders.all { it.isValid() }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

