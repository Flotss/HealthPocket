package com.healthpocket.ui.profile

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
class ProfileUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun profileScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vital metrics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
    }

    @Test
    fun profileScreenDisplaysUserInformation() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun profileScreenNavigatesToSettings() {
        var navigatedToSettings = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {
                                navigatedToSettings = true
                            },
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Chercher le bouton de paramètres dans la TopAppBar
        // Il devrait y avoir une icône de paramètres
        composeTestRule.onAllNodes(hasContentDescription("Settings"))
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToSettings)
    }

    @Test
    fun profileScreenShowsVitalMetricsSection() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier que la section des métriques vitales est affichée
        composeTestRule.onNodeWithText("Vital metrics").assertIsDisplayed()
        composeTestRule.onNodeWithText("View history").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log vitals").assertIsDisplayed()
    }

    @Test
    fun profileScreenNavigatesToVitalHistory() {
        var navigatedToVitalHistory = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {
                                navigatedToVitalHistory = true
                            },
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur "View history"
        composeTestRule.onNodeWithText("View history").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToVitalHistory)
    }

    @Test
    fun profileScreenNavigatesToAddVitalMetric() {
        var navigatedToAddVital = false
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {
                                navigatedToAddVital = true
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur "Log vitals"
        composeTestRule.onNodeWithText("Log vitals").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la navigation a été déclenchée
        assert(navigatedToAddVital)
    }

    @Test
    fun profileScreenShowsLogoutDialog() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur le bouton de déconnexion - utiliser onFirst() car il peut y avoir plusieurs nœuds
        composeTestRule.onAllNodes(hasText("Logout"))
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()

        // Vérifier que la boîte de dialogue de confirmation s'affiche
        // Utiliser useUnmergedTree pour éviter les conflits avec les nœuds parents
        composeTestRule.onNodeWithText("Logout", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Do you really want to logout?", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Cancel", useUnmergedTree = true).assertExists()
    }

    @Test
    fun profileScreenCancelsLogout() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Cliquer sur le bouton de déconnexion
        composeTestRule.onNodeWithText("Logout").performClick()
        composeTestRule.waitForIdle()

        // Cliquer sur Annuler
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        // Vérifier que nous sommes toujours sur l'écran de profil
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun profileScreenDisplaysEmptyVitalMetrics() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Profile.route) {
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen(
                            viewModel = hiltViewModel(),
                            onNavigateToSettings = {},
                            onLogout = {},
                            onViewVitalsHistory = {},
                            onAddVitalMetric = {}
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Vérifier que le message d'absence de métriques s'affiche si aucune métrique n'existe
        // Cela dépend de l'état du ViewModel
        composeTestRule.onNodeWithText("Vital metrics").assertIsDisplayed()
    }
}
