package com.healthpocket.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.healthpocket.ui.appointments.AddAppointmentScreen
import com.healthpocket.ui.appointments.AppointmentsScreen
import com.healthpocket.ui.auth.LoginScreen
import com.healthpocket.ui.auth.RegisterScreen
import com.healthpocket.ui.home.HomeScreen
import com.healthpocket.ui.journal.JournalScreen
import com.healthpocket.ui.medications.AddMedicationScreen
import com.healthpocket.ui.medications.EditMedicationScreen
import com.healthpocket.ui.medications.MedicationDetailScreen
import com.healthpocket.ui.medications.MedicationsScreen
import com.healthpocket.ui.navigation.BottomNavItem
import com.healthpocket.ui.navigation.NavRoutes
import com.healthpocket.ui.profile.ProfileScreen
import com.healthpocket.ui.settings.SettingsScreen
import com.healthpocket.ui.settings.SettingsViewModel

/**
 * Main composable for the HealthPocket application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthPocketApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val isLoggedIn by settingsViewModel.isLoggedIn.collectAsState(initial = false)

    // Redirect to login if user becomes logged out (e.g., token expired or invalid)
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentDestination?.route != NavRoutes.Login.route && currentDestination?.route != NavRoutes.Register.route) {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Bottom nav items
    val bottomNavItems = listOf(
        BottomNavItem.HOME,
        BottomNavItem.MEDICATIONS,
        BottomNavItem.APPOINTMENTS,
        BottomNavItem.JOURNAL,
        BottomNavItem.PROFILE
    )

    // Check if current route should show bottom nav
    val showBottomNav = currentDestination?.let { dest ->
        bottomNavItems.any { item -> 
            dest.hierarchy.any { it.route == item.route } || dest.route == item.route
        }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomNav && isLoggedIn) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                when (item) {
                                    BottomNavItem.HOME -> Icon(Icons.Filled.Home, contentDescription = null)
                                    BottomNavItem.MEDICATIONS -> Icon(Icons.Filled.Medication, contentDescription = null)
                                    BottomNavItem.APPOINTMENTS -> Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                                    BottomNavItem.JOURNAL -> Icon(Icons.Filled.EditNote, contentDescription = null)
                                    BottomNavItem.PROFILE -> Icon(Icons.Filled.Person, contentDescription = null)
                                }
                            },
                            label = { Text(stringResource(item.titleResId)) },
                            selected = currentDestination?.let { dest ->
                                dest.hierarchy.any { it.route == item.route } || dest.route == item.route
                            } == true,
                            onClick = {
                                val currentRoute = currentDestination?.route
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) NavRoutes.Home.route else NavRoutes.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth routes
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.Register.route)
                    }
                )
            }

            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Main routes - Home must be first
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    onNavigateToMedications = { navController.navigate(NavRoutes.Medications.route) },
                    onNavigateToAppointments = { navController.navigate(NavRoutes.Appointments.route) },
                    onNavigateToJournal = { navController.navigate(NavRoutes.Journal.route) }
                )
            }

            composable(NavRoutes.AddMedication.route) {
                AddMedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.MedicationDetail.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.StringType })
            ) {
                MedicationDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { medicationId ->
                        navController.navigate(NavRoutes.EditMedication.createRoute(medicationId))
                    }
                )
            }

            composable(
                route = NavRoutes.EditMedication.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.StringType })
            ) {
                EditMedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Medications.route) {
                MedicationsScreen(
                    onNavigateToAddMedication = { navController.navigate(NavRoutes.AddMedication.route) },
                    onNavigateToMedicationDetail = { medicationId ->
                        navController.navigate(NavRoutes.MedicationDetail.createRoute(medicationId))
                    }
                )
            }

            composable(NavRoutes.Appointments.route) {
                AppointmentsScreen(
                    onNavigateToAddAppointment = { navController.navigate(NavRoutes.AddAppointment.route) },
                    onNavigateToAppointmentDetail = { appointmentId ->
                        navController.navigate(NavRoutes.AppointmentDetail.createRoute(appointmentId))
                    }
                )
            }

            composable(NavRoutes.AddAppointment.route) {
                AddAppointmentScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Journal.route) {
                JournalScreen()
            }

            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onLogout = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

