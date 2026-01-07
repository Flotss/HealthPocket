package com.healthpocket.ui.appointments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAppointmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }
    
    // Date/Time state
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_appointment)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Filled.ArrowBack, contentDescription = "Back")
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
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.appointment_title)) },
                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Doctor name
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text(stringResource(R.string.doctor_name)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(R.string.location)) },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date display (simplified - in production, use DatePicker)
            OutlinedTextField(
                value = selectedDate.toString(),
                onValueChange = { },
                label = { Text(stringResource(R.string.date)) },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Time display (simplified - in production, use TimePicker)
            OutlinedTextField(
                value = selectedTime.toString(),
                onValueChange = { },
                label = { Text(stringResource(R.string.time)) },
                leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enable_reminder),
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
                    .height(100.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val appointmentDateTime = selectedDate.atTime(selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    viewModel.saveAppointment(
                        title = title,
                        doctorName = doctorName.ifBlank { null },
                        location = location.ifBlank { null },
                        description = description.ifBlank { null },
                        appointmentDate = appointmentDateTime,
                        reminderEnabled = reminderEnabled,
                        notes = notes.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
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

