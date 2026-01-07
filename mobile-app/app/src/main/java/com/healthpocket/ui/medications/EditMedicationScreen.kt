package com.healthpocket.ui.medications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.model.MedicationReminder
import com.healthpocket.ui.theme.MedicationColors
import com.healthpocket.util.ColorUtils
import com.healthpocket.util.remindersToScheduleTimes
import com.healthpocket.util.scheduleTimesToReminders
import java.time.LocalDate

/**
 * Screen for editing an existing medication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_medication)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Filled.ArrowBack, contentDescription = "Back")
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
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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
                EditMedicationForm(
                    medication = uiState.medication!!,
                    isLoading = uiState.isSaving,
                    onSave = { name, dosage, frequency, scheduleTimes, endDate, notes, color, reminderEnabled ->
                        viewModel.saveMedication(
                            name = name,
                            dosage = dosage,
                            frequency = frequency,
                            scheduleTimes = scheduleTimes,
                            endDate = endDate,
                            notes = notes,
                            color = color,
                            reminderEnabled = reminderEnabled
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun EditMedicationForm(
    medication: MedicationEntity,
    isLoading: Boolean,
    onSave: (
        name: String,
        dosage: String,
        frequency: String,
        scheduleTimes: List<String>,
        endDate: LocalDate?,
        notes: String?,
        color: String,
        reminderEnabled: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(medication.name) }
    var dosage by remember { mutableStateOf(medication.dosage) }
    var frequency by remember { mutableStateOf(medication.frequency) }
    
    // Convert existing scheduleTimes to reminders
    val initialScheduleTimes = medication.scheduleTimes.split(",").filter { it.isNotBlank() }
    val initialReminders = remember(medication.id) {
        if (medication.reminderEnabled && initialScheduleTimes.isNotEmpty()) {
            scheduleTimesToReminders(initialScheduleTimes)
        } else emptyList()
    }
    
    var reminders by remember { mutableStateOf(initialReminders) }
    var notes by remember { mutableStateOf(medication.notes ?: "") }
    var selectedColorIndex by remember {
        mutableIntStateOf(
            MedicationColors.indexOfFirst {
                try {
                    android.graphics.Color.parseColor(medication.color) == android.graphics.Color.parseColor(
                        it.value.toString()
                    )
                } catch (e: Exception) {
                    false
                }
            }.let { if (it == -1) 0 else it }
        )
    }

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
        isLoading = isLoading,
        onSave = {
            // Convert reminders to scheduleTimes format with days
            val scheduleTimes = remindersToScheduleTimes(reminders)
            
            onSave(
                name,
                dosage,
                frequency,
                scheduleTimes,
                medication.endDate,
                notes.ifBlank { null },
                ColorUtils.getMedicationColorHex(selectedColorIndex),
                reminders.isNotEmpty() && reminders.all { it.isValid() }
            )
        },
        modifier = modifier
    )
}

