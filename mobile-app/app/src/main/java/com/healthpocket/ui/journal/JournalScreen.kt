package com.healthpocket.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.ui.theme.MoodBad
import com.healthpocket.ui.theme.MoodExcellent
import com.healthpocket.ui.theme.MoodGood
import com.healthpocket.ui.theme.MoodNeutral
import com.healthpocket.ui.theme.MoodVeryBad
import java.time.format.DateTimeFormatter

/**
 * Journal screen for health logging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_journal)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.how_are_you_feeling),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MoodButton(
                            mood = 1,
                            emoji = "😢",
                            selected = uiState.selectedMood == 1,
                            onClick = { viewModel.setMood(1) }
                        )
                        MoodButton(
                            mood = 2,
                            emoji = "😕",
                            selected = uiState.selectedMood == 2,
                            onClick = { viewModel.setMood(2) }
                        )
                        MoodButton(
                            mood = 3,
                            emoji = "😐",
                            selected = uiState.selectedMood == 3,
                            onClick = { viewModel.setMood(3) }
                        )
                        MoodButton(
                            mood = 4,
                            emoji = "🙂",
                            selected = uiState.selectedMood == 4,
                            onClick = { viewModel.setMood(4) }
                        )
                        MoodButton(
                            mood = 5,
                            emoji = "😊",
                            selected = uiState.selectedMood == 5,
                            onClick = { viewModel.setMood(5) }
                        )
                    }

                    if (uiState.selectedMood != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.saveTodayLog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.save_mood))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.recent_entries),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recentLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_journal_entries),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentLogs) { log ->
                        JournalEntryCard(log = log)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddJournalEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { mood, energyLevel, sleepQuality, sleepHours, notes ->
                viewModel.saveDetailedLog(mood, energyLevel, sleepQuality, sleepHours, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MoodButton(
    mood: Int,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) {
                when (mood) {
                    1 -> MoodVeryBad
                    2 -> MoodBad
                    3 -> MoodNeutral
                    4 -> MoodGood
                    5 -> MoodExcellent
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.size(56.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun JournalEntryCard(log: HealthLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = log.logDate.format(DateTimeFormatter.ofPattern("dd")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = log.logDate.format(DateTimeFormatter.ofPattern("MMM")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                log.mood?.let { mood ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.mood_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (mood) {
                                1 -> "😢"
                                2 -> "😕"
                                3 -> "😐"
                                4 -> "🙂"
                                5 -> "😊"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                log.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun AddJournalEntryDialog(
    onDismiss: () -> Unit,
    onSave: (mood: Int?, energyLevel: Int?, sleepQuality: Int?, sleepHours: Float?, notes: String?) -> Unit
) {
    var mood by remember { mutableIntStateOf(3) }
    var energyLevel by remember { mutableIntStateOf(3) }
    var sleepQuality by remember { mutableIntStateOf(3) }
    var sleepHours by remember { mutableStateOf("7.0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_journal_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.mood_label))
                Slider(
                    value = mood.toFloat(),
                    onValueChange = { mood = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Text(stringResource(R.string.energy_level))
                Slider(
                    value = energyLevel.toFloat(),
                    onValueChange = { energyLevel = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Text(stringResource(R.string.sleep_quality))
                Slider(
                    value = sleepQuality.toFloat(),
                    onValueChange = { sleepQuality = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                OutlinedTextField(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    label = { Text(stringResource(R.string.sleep_hours)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    mood,
                    energyLevel,
                    sleepQuality,
                    sleepHours.toFloatOrNull(),
                    notes.ifBlank { null })
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

