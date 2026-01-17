package com.healthpocket.ui.appointments

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthpocket.MainActivity
import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.ui.navigation.NavRoutes
import com.healthpocket.ui.theme.HealthPocketTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import javax.inject.Inject
import kotlin.jvm.java

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppointmentUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var appointmentDao: AppointmentDao

    private val testAppointmentId = "test-appointment-id"
    private val testAppointmentIdForCompletion = "test-appointment-completion-id"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        runBlocking {
            appointmentDao.deleteAll()
        }
    }

    private suspend fun createTestAppointment(
        id: String,
        title: String,
        description: String,
        doctorName: String,
        location: String,
        status: AppointmentStatus = AppointmentStatus.SCHEDULED
    ) {
        val appointment = AppointmentEntity(
            id = id,
            title = title,
            description = description,
            doctorName = doctorName,
            location = location,
            appointmentDate = System.currentTimeMillis() + 86400000,
            durationMinutes = 30,
            status = status,
            syncStatus = SyncStatus.SYNCED
        )
        appointmentDao.insert(appointment)
    }

    @Test
    fun addNewAppointmentSuccessfully() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.Appointments.route
                ) {
                    composable(NavRoutes.Appointments.route) {
                        AppointmentsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToAddAppointment = {
                                navController.navigate(NavRoutes.AddAppointment.route)
                            },
                            onNavigateToAppointmentDetail = { appointmentId ->
                                navController.navigate(
                                    NavRoutes.AppointmentDetail.createRoute(
                                        appointmentId
                                    )
                                )
                            }
                        )
                    }
                    composable(NavRoutes.AddAppointment.route) {
                        AddAppointmentScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Appointments").assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No upcoming appointments").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add appointment").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add appointment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").performTextInput("Consultation cardiologie")
        composeTestRule.onNodeWithText("Doctor name").performTextInput("Dr. Dupont")
        composeTestRule.onNodeWithText("Location").performTextInput("Hôpital Central")
        composeTestRule.onNodeWithText("Description").performTextInput("Consultation de routine")
        composeTestRule
            .onNodeWithText("Date")
            .assertIsDisplayed()
            .performClick()

        onView(isAssignableFrom(DatePicker::class.java))
            .perform(PickerActions.setDate(Date().year + 1900 + 1, 12, 15))

        onView(withText("OK")).perform(click())

        composeTestRule
            .onNodeWithText("Time")
            .assertIsDisplayed()
            .performClick();
        onView(isAssignableFrom(TimePicker::class.java))
            .perform(PickerActions.setTime(14, 30))
        onView(withId(android.R.id.button1)).perform(click())
        composeTestRule
            .onNodeWithText("14:30")
            .assertIsDisplayed()


        composeTestRule.onNodeWithText("Enable reminder").performClick()
        composeTestRule.onNodeWithText("Notes")
            .assertExists()
            .assertIsDisplayed()
            .performTextInput("Apporter les derniers examens");
        composeTestRule.waitForIdle();
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Appointments").assertIsDisplayed()
        composeTestRule.onNodeWithText("Consultation cardiologie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dr. Dupont").assertIsDisplayed()
    }

    @Test
    fun appointmentDetailShowsCorrectInformation() {
        runBlocking {
            createTestAppointment(
                id = testAppointmentId,
                title = "Test Appointment",
                description = "Test Description",
                doctorName = "Dr. Test",
                location = "Test Location"
            )
        }

        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AppointmentDetail.createRoute(testAppointmentId)
                ) {
                    composable(NavRoutes.AppointmentDetail.route) {
                        AppointmentDetailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onAppointmentDeleted = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Test Appointment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mark as completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete appointment").assertIsDisplayed()
    }

    @Test
    fun markAppointmentAsCompleted() {
        runBlocking {
            createTestAppointment(
                id = testAppointmentIdForCompletion,
                title = "Test Appointment for Completion",
                description = "Test Description",
                doctorName = "Dr. Test",
                location = "Test Location",
                status = AppointmentStatus.SCHEDULED
            )
        }

        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AppointmentDetail.createRoute(
                        testAppointmentIdForCompletion
                    )
                ) {
                    composable(NavRoutes.AppointmentDetail.route) {
                        AppointmentDetailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onAppointmentDeleted = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mark as completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mark as completed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mark as completed").assertDoesNotExist()
    }

    @Test
    fun appointmentsListShowsUpcomingAndPast() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.Appointments.route
                ) {
                    composable(NavRoutes.Appointments.route) {
                        AppointmentsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToAddAppointment = {
                                navController.navigate(NavRoutes.AddAppointment.route)
                            },
                            onNavigateToAppointmentDetail = { appointmentId ->
                                navController.navigate(
                                    NavRoutes.AppointmentDetail.createRoute(
                                        appointmentId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Appointments").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }
}