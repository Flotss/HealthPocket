package com.healthpocket.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.healthpocket.data.local.entity.IntakeStatus
import com.healthpocket.data.local.entity.MedicationIntakeEntity

/**
 * Home screen showing dashboard with quick access cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToJournal: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = stringResource(R.string.welcome_back),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = uiState.userName.ifBlank { stringResource(R.string.app_name) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's medications card
            item {
                QuickAccessCard(
                    title = stringResource(R.string.todays_medications),
                    icon = Icons.Filled.Medication,
                    count = uiState.pendingMedicationsCount,
                    subtitle = stringResource(R.string.pending_intakes, uiState.pendingMedicationsCount),
                    onClick = onNavigateToMedications,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            // Upcoming appointments card
            item {
                QuickAccessCard(
                    title = stringResource(R.string.upcoming_appointments),
                    icon = Icons.Filled.CalendarMonth,
                    count = uiState.upcomingAppointmentsCount,
                    subtitle = stringResource(R.string.this_week),
                    onClick = onNavigateToAppointments,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            // Health journal card
            item {
                QuickAccessCard(
                    title = stringResource(R.string.health_journal),
                    icon = Icons.Filled.EditNote,
                    count = null,
                    subtitle = if (uiState.todayLogExists) 
                        stringResource(R.string.today_logged) 
                    else 
                        stringResource(R.string.log_today),
                    onClick = onNavigateToJournal,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            // Today's pending medications
            if (uiState.todaysPendingIntakes.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.medications_to_take),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.todaysPendingIntakes.take(5)) { intake ->
                    MedicationIntakeCard(
                        intake = intake,
                        medicationName = uiState.medicationNames[intake.medicationId] ?: "",
                        onMarkTaken = { viewModel.markIntakeAsTaken(intake) },
                        onMarkSkipped = { viewModel.markIntakeAsSkipped(intake) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int?,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (count != null && count > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(count.toString())
                }
            }
        }
    }
}

@Composable
fun MedicationIntakeCard(
    intake: MedicationIntakeEntity,
    medicationName: String,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicationName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(intake.scheduledTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (intake.status == IntakeStatus.PENDING) {
                IconButton(onClick = onMarkSkipped) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                FilledIconButton(onClick = onMarkTaken) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Take"
                    )
                }
            } else {
                Icon(
                    imageVector = when (intake.status) {
                        IntakeStatus.TAKEN -> Icons.Filled.CheckCircle
                        IntakeStatus.SKIPPED -> Icons.Filled.Cancel
                        else -> Icons.Filled.Schedule
                    },
                    contentDescription = null,
                    tint = when (intake.status) {
                        IntakeStatus.TAKEN -> MaterialTheme.colorScheme.primary
                        IntakeStatus.SKIPPED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

