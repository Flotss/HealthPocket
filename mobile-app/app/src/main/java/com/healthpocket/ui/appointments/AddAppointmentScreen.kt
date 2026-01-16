package com.healthpocket.ui.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAppointmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val dateFormatter =
        remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }

    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLocationDropdownExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }

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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.appointment_title)) },
                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text(stringResource(R.string.doctor_name)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val locationSuggestions = uiState.locationSuggestions
            val isLocationLookupLoading = uiState.isLocationLookupInProgress
            val locationLookupError = uiState.locationLookupError
            val showNoLocationResults = uiState.showNoLocationResults
            val shouldShowLocationMenu = isLocationDropdownExpanded &&
                    (isLocationLookupLoading || locationSuggestions.isNotEmpty() || locationLookupError != null || showNoLocationResults)

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        isLocationDropdownExpanded = it.length >= 3
                        viewModel.searchLocationSuggestions(it)
                    },
                    label = { Text(stringResource(R.string.location)) },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = shouldShowLocationMenu,
                    onDismissRequest = {
                        isLocationDropdownExpanded = false
                        viewModel.clearLocationSuggestions()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    properties = PopupProperties(focusable = false)
                ) {
                    when {
                        isLocationLookupLoading -> {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(text = stringResource(R.string.searching_locations))
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                        }

                        locationLookupError != null -> {
                            val displayedError = locationLookupError.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.locations_load_failed)
                            DropdownMenuItem(
                                text = { Text(displayedError) },
                                onClick = {},
                                enabled = false
                            )
                        }

                        showNoLocationResults -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.no_locations_found)) },
                                onClick = {},
                                enabled = false
                            )
                        }

                        else -> {
                            locationSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        location = suggestion
                                        isLocationDropdownExpanded = false
                                        viewModel.clearLocationSuggestions()
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            PickerTextField(
                value = selectedDate.format(dateFormatter),
                label = stringResource(R.string.date),
                icon = Icons.Filled.CalendarToday,
                modifier = Modifier.fillMaxWidth()
            ) {
                focusManager.clearFocus()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    },
                    selectedDate.year,
                    selectedDate.monthValue - 1,
                    selectedDate.dayOfMonth
                ).show()
            }

            PickerTextField(
                value = selectedTime.format(timeFormatter),
                label = stringResource(R.string.time),
                icon = Icons.Filled.Schedule,
                modifier = Modifier.fillMaxWidth()
            ) {
                focusManager.clearFocus()
                TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        selectedTime = LocalTime.of(hour, minute)
                    },
                    selectedTime.hour,
                    selectedTime.minute,
                    DateFormat.is24HourFormat(context)
                ).show()
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

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

@Composable
private fun PickerTextField(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
