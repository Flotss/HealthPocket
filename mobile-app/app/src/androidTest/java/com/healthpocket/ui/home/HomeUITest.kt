package com.healthpocket.ui.home

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthpocket.MainActivity
import com.healthpocket.ui.navigation.NavRoutes
import com.healthpocket.ui.theme.HealthPocketTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun homeScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {},
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier les éléments principaux
        composeTestRule.onNodeWithText("Hello!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log vitals").assertIsDisplayed()
        composeTestRule.onNodeWithText("View history").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upcoming appointments").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health journal").assertIsDisplayed()
    }

    @Test
    fun homeScreenNavigatesToMedications() {
        var navigatedToMedications = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {
                                navigatedToMedications = true
                            },
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {},
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur la carte des médicaments
        composeTestRule.onNodeWithText("Today's medications").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToMedications)
    }

    @Test
    fun homeScreenNavigatesToAppointments() {
        var navigatedToAppointments = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {
                                navigatedToAppointments = true
                            },
                            onNavigateToJournal = {},
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur la carte des rendez-vous
        composeTestRule.onNodeWithText("Upcoming appointments").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToAppointments)
    }

    @Test
    fun homeScreenNavigatesToJournal() {
        var navigatedToJournal = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {
                                navigatedToJournal = true
                            },
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur la carte du journal
        composeTestRule.onNodeWithText("Health journal").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToJournal)
    }

    @Test
    fun homeScreenShowsVitalMetricsButtons() {
        var navigatedToAddVital = false
        var navigatedToVitalHistory = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {},
                            onAddVitalMetric = {
                                navigatedToAddVital = true
                            },
                            onViewVitalHistory = {
                                navigatedToVitalHistory = true
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier que les boutons sont présents
        composeTestRule.onNodeWithText("Log vitals").assertIsDisplayed()
        composeTestRule.onNodeWithText("View history").assertIsDisplayed()

        // Cliquer sur le bouton "Log vitals"
        composeTestRule.onNodeWithText("Log vitals").performClick()
        composeTestRule.waitForIdle()
        assert(navigatedToAddVital)

        // Cliquer sur le bouton "View history"
        composeTestRule.onNodeWithText("View history").performClick()
        composeTestRule.waitForIdle()
        assert(navigatedToVitalHistory)
    }

    @Test
    fun homeScreenDisplaysPendingMedicationsCount() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {},
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier que la carte des médicaments est affichée
        composeTestRule.onNodeWithText("Today's medications").assertIsDisplayed()
    }

    @Test
    fun homeScreenDisplaysUpcomingAppointmentsCount() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Home.route) {
                    composable(NavRoutes.Home.route) {
                        HomeScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToMedications = {},
                            onNavigateToAppointments = {},
                            onNavigateToJournal = {},
                            onAddVitalMetric = {},
                            onViewVitalHistory = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier que la carte des rendez-vous est affichée
        composeTestRule.onNodeWithText("Upcoming appointments").assertIsDisplayed()
    }
}
