package com.healthpocket.ui.medications

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthpocket.R
import com.healthpocket.data.model.MedicationReminder
import com.healthpocket.ui.theme.MedicationColors
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Shared form component for adding and editing medications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationForm(
    name: String,
    onNameChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    reminders: List<MedicationReminder>,
    onRemindersChange: (List<MedicationReminder>) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    selectedColorIndex: Int,
    onColorIndexChange: (Int) -> Unit,
    isLoading: Boolean = false,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medication name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.medication_name)) },
            leadingIcon = { Icon(Icons.Filled.Medication, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Dosage
        OutlinedTextField(
            value = dosage,
            onValueChange = onDosageChange,
            label = { Text(stringResource(R.string.dosage)) },
            placeholder = { Text(stringResource(R.string.dosage_example)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Frequency
        OutlinedTextField(
            value = frequency,
            onValueChange = onFrequencyChange,
            label = { Text(stringResource(R.string.frequency)) },
            placeholder = { Text(stringResource(R.string.frequency_example)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Reminders section
        Text(
            text = stringResource(R.string.reminders),
            style = MaterialTheme.typography.titleMedium
        )

        if (reminders.isEmpty()) {
            Text(
                text = stringResource(R.string.no_reminders_add_one),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        reminders.forEachIndexed { index, reminder ->
            ReminderCard(
                reminder = reminder,
                index = index,
                onReminderChange = { updated ->
                    val updatedReminders = reminders.toMutableList()
                    updatedReminders[index] = updated
                    onRemindersChange(updatedReminders)
                },
                onDelete = {
                    onRemindersChange(reminders.filterIndexed { i, _ -> i != index })
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Add reminder button
        OutlinedButton(
            onClick = {
                val newReminder = MedicationReminder(
                    days = emptySet(),
                    times = listOf(LocalTime.of(7, 0)) // Default 7h
                )
                onRemindersChange(reminders + newReminder)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_reminder))
        }

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
                    onClick = { onColorIndexChange(index) },
                    label = { Text("") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = color,
                        selectedContainerColor = color
                    ),
                    modifier = Modifier.size(40.dp),
                    enabled = !isLoading
                )
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() 
                && dosage.isNotBlank() 
                && frequency.isNotBlank() 
                && reminders.isNotEmpty()
                && reminders.all { it.isValid() }
                && !isLoading
        ) {
            if (isLoading) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: MedicationReminder,
    index: Int,
    onReminderChange: (MedicationReminder) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reminder) + " ${index + 1}",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = onDelete,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Week picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.days_of_week),
                    style = MaterialTheme.typography.labelMedium
                )
                val allDays = DayOfWeek.entries.toSet()
                val allSelected = reminder.days.size == allDays.size
                TextButton(
                    onClick = {
                        if (allSelected) {
                            onReminderChange(reminder.copy(days = emptySet()))
                        } else {
                            onReminderChange(reminder.copy(days = allDays))
                        }
                    },
                    enabled = enabled
                ) {
                    Text(
                        text = if (allSelected) {
                            stringResource(R.string.deselect_all_days)
                        } else {
                            stringResource(R.string.select_all_days)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            WeekPicker(
                selectedDays = reminder.days,
                onDaysChange = { days ->
                    onReminderChange(reminder.copy(days = days))
                },
                enabled = enabled
            )

            // Times
            Text(
                text = stringResource(R.string.times),
                style = MaterialTheme.typography.labelMedium
            )
            
            reminder.times.forEachIndexed { timeIndex, time ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showTimePicker = timeIndex },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    ) {
                        Text(time.toString())
                    }
                    if (reminder.times.size > 1) {
                        IconButton(
                            onClick = {
                                val updatedTimes = reminder.times.toMutableList()
                                updatedTimes.removeAt(timeIndex)
                                onReminderChange(reminder.copy(times = updatedTimes))
                            },
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Add time button
            OutlinedButton(
                onClick = {
                    val newTime = LocalTime.of(7, 0) // Default 7h
                    onReminderChange(reminder.copy(times = reminder.times + newTime))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_time))
            }

            // Time picker dialog
            showTimePicker?.let { timeIndex ->
                TimePickerDialog(
                    initialTime = reminder.times[timeIndex],
                    onTimeSelected = { time ->
                        val updatedTimes = reminder.times.toMutableList()
                        updatedTimes[timeIndex] = time
                        onReminderChange(reminder.copy(times = updatedTimes))
                        showTimePicker = null
                    },
                    onDismiss = { showTimePicker = null }
                )
            }
        }
    }
}

@Composable
fun WeekPicker(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit,
    enabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val dayLabels = mapOf(
        DayOfWeek.MONDAY to stringResource(R.string.monday_short),
        DayOfWeek.TUESDAY to stringResource(R.string.tuesday_short),
        DayOfWeek.WEDNESDAY to stringResource(R.string.wednesday_short),
        DayOfWeek.THURSDAY to stringResource(R.string.thursday_short),
        DayOfWeek.FRIDAY to stringResource(R.string.friday_short),
        DayOfWeek.SATURDAY to stringResource(R.string.saturday_short),
        DayOfWeek.SUNDAY to stringResource(R.string.sunday_short)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DayOfWeek.entries.forEach { day ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = {
                    val updatedDays = selectedDays.toMutableSet()
                    if (updatedDays.contains(day)) {
                        updatedDays.remove(day)
                    } else {
                        updatedDays.add(day)
                    }
                    onDaysChange(updatedDays)
                },
                label = {
                    Text(dayLabels[day]?.take(1) ?: "")},
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTime by remember { mutableStateOf(initialTime) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.hour),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val newHour = (selectedTime.hour - 1).let {
                                        if (it < 0) 23 else it
                                    }
                                    selectedTime = LocalTime.of(newHour, selectedTime.minute)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineSmall)
                            }
                            Text(
                                text = selectedTime.hour.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(
                                onClick = {
                                    val newHour = (selectedTime.hour + 1) % 24
                                    selectedTime = LocalTime.of(newHour, selectedTime.minute)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    // Minute picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.minute),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val newMinute = (selectedTime.minute - 15).let {
                                        when {
                                            it < 0 -> 60 + it
                                            else -> it
                                        }
                                    }
                                    selectedTime = LocalTime.of(selectedTime.hour, newMinute)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineSmall)
                            }
                            Text(
                                text = selectedTime.minute.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(
                                onClick = {
                                    val newMinute = (selectedTime.minute + 15) % 60
                                    selectedTime = LocalTime.of(selectedTime.hour, newMinute)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(selectedTime) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
