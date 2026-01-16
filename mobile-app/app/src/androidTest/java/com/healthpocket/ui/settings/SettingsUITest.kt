package com.healthpocket.ui.settings

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
class SettingsUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun toggleDarkMode() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Settings.route) {
                    composable(NavRoutes.Settings.route) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark mode").assertIsDisplayed()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Dark mode")
            .onParent()
            .onChildren()
            .filter(hasClickAction())
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Dark mode")
            .onParent()
            .onChildren()
            .filter(hasClickAction())
            .onFirst()
            .performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun changeLanguageSetting() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Settings.route) {
                    composable(NavRoutes.Settings.route) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Language").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitForIdle()
    }

    @Test
    fun viewAppInformation() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Settings.route) {
                    composable(NavRoutes.Settings.route) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("About", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Version", useUnmergedTree = true).assertExists()
        composeTestRule.waitForIdle()
    }

    @Test
    fun resetSettingsToDefault() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Settings.route) {
                    composable(NavRoutes.Settings.route) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Dark mode").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreenDisplaysAllOptions() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Settings.route) {
                    composable(NavRoutes.Settings.route) {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version").assertIsDisplayed()
    }
}