package com.healthpocket.ui.navigation

/**
 * Navigation routes for the HealthPocket app.
 */
sealed class NavRoutes(val route: String) {
    // Auth routes
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    
    // Main routes (bottom nav)
    object Home : NavRoutes("home")
    object Medications : NavRoutes("medications")
    object Appointments : NavRoutes("appointments")
    object Journal : NavRoutes("journal")
    object Profile : NavRoutes("profile")
    
    // Detail routes
    object MedicationDetail : NavRoutes("medication/{medicationId}") {
        fun createRoute(medicationId: String) = "medication/$medicationId"
    }
    object AddMedication : NavRoutes("medication/add")
    
    object AppointmentDetail : NavRoutes("appointment/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment/$appointmentId"
    }
    object AddAppointment : NavRoutes("appointment/add")
    
    object JournalEntry : NavRoutes("journal/{date}") {
        fun createRoute(date: String) = "journal/$date"
    }
    
    object AddVitalMetric : NavRoutes("vitals/add/{type}") {
        fun createRoute(type: String) = "vitals/add/$type"
    }
    
    object Settings : NavRoutes("settings")
}

/**
 * Bottom navigation items.
 */
enum class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: String
) {
    HOME(NavRoutes.Home.route, com.healthpocket.R.string.nav_home, "home"),
    MEDICATIONS(NavRoutes.Medications.route, com.healthpocket.R.string.nav_medications, "medication"),
    APPOINTMENTS(NavRoutes.Appointments.route, com.healthpocket.R.string.nav_appointments, "calendar_month"),
    JOURNAL(NavRoutes.Journal.route, com.healthpocket.R.string.nav_journal, "edit_note"),
    PROFILE(NavRoutes.Profile.route, com.healthpocket.R.string.nav_profile, "person")
}

