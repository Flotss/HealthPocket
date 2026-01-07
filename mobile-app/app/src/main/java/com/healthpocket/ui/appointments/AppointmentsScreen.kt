package com.healthpocket.ui.appointments

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.R
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying list of appointments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToAppointmentDetail: (String) -> Unit,
    viewModel: AppointmentsViewModel = hiltViewModel()
) {
    val appointments by viewModel.upcomingAppointments.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appointments)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAppointment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add appointment")
            }
        }
    ) { paddingValues ->
        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_appointments),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToAddAppointment) {
                        Text(stringResource(R.string.add_first_appointment))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onNavigateToAppointmentDetail(appointment.id) },
                        onMarkCompleted = { viewModel.markAsCompleted(appointment.id) },
                        onDelete = { viewModel.deleteAppointment(appointment) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Date box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = SimpleDateFormat("dd", Locale.getDefault())
                            .format(Date(appointment.appointmentDate)),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = SimpleDateFormat("MMM", Locale.getDefault())
                            .format(Date(appointment.appointmentDate)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (!appointment.doctorName.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.doctorName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormat.format(Date(appointment.appointmentDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (!appointment.location.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status chip
                if (appointment.status != AppointmentStatus.SCHEDULED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (appointment.status) {
                                    AppointmentStatus.COMPLETED -> stringResource(R.string.completed)
                                    AppointmentStatus.CANCELLED -> stringResource(R.string.cancelled)
                                    else -> ""
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (appointment.status) {
                                AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    )
                }
            }
            
            // Action buttons
            if (appointment.status == AppointmentStatus.SCHEDULED) {
                Column {
                    IconButton(onClick = onMarkCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Complete",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

