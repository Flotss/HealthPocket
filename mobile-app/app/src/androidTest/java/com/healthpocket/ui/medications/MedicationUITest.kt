package com.healthpocket.ui.medications

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthpocket.MainActivity
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.entity.MedicationEntity
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
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MedicationUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var medicationDao: MedicationDao

    private val testMedicationId = "test-medication-id"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        runBlocking {
            medicationDao.deleteAll()
        }
    }

    @Test
    fun addNewMedicationSuccessfully() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Medications.route) {
                    composable(NavRoutes.Medications.route) {
                        MedicationsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToAddMedication = {
                                navController.navigate(NavRoutes.AddMedication.route)
                            },
                            onNavigateToMedicationDetail = { medicationId ->
                                navController.navigate(NavRoutes.MedicationDetail.createRoute(medicationId))
                            }
                        )
                    }
                    composable(NavRoutes.AddMedication.route) {
                        AddMedicationScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("No medications").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add medication").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add medication").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medication name").performTextInput("Aspirine")
        composeTestRule.onNodeWithText("Dosage").performTextInput("500mg")
        composeTestRule.onNodeWithText("Frequency").performTextInput("1 fois par jour")
        composeTestRule.onNodeWithText("Add reminder").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("M").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Save").performScrollTo()
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(
            condition = {
                composeTestRule.onAllNodesWithText("Medications").fetchSemanticsNodes().size == 1
            },
            timeoutMillis = 10_000
        )

        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aspirine").assertIsDisplayed()
        composeTestRule.onNodeWithText("500mg • 1 fois par jour").assertIsDisplayed()
    }

    @Test
    fun medicationDetailShowsCorrectInformation() {
        runBlocking {
            val medication = MedicationEntity(
                id = testMedicationId,
                name = "Test Medication",
                dosage = "500mg",
                frequency = "Daily",
                scheduleTimes = "08:00",
                startDate = LocalDate.now(),
                isActive = true,
                syncStatus = SyncStatus.SYNCED
            )
            medicationDao.insert(medication)
        }
        
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.MedicationDetail.createRoute("test-medication-id")) {
                    composable(NavRoutes.MedicationDetail.route) {
                        MedicationDetailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToEdit = { medId ->
                                navController.navigate(NavRoutes.EditMedication.createRoute(medId))
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Medication detail").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    @Test
    fun toggleMedicationStatus() {
        runBlocking {
            val medication = MedicationEntity(
                id = testMedicationId,
                name = "Test Medication",
                dosage = "500mg",
                frequency = "Daily",
                scheduleTimes = "08:00",
                startDate = LocalDate.now(),
                isActive = true,
                syncStatus = SyncStatus.SYNCED
            )
            medicationDao.insert(medication)
        }
        
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.MedicationDetail.createRoute("test-medication-id")) {
                    composable(NavRoutes.MedicationDetail.route) {
                        MedicationDetailScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToEdit = { medId ->
                                navController.navigate(NavRoutes.EditMedication.createRoute(medId))
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Active", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Deactivate", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Inactive", useUnmergedTree = true).assertExists()
    }
}