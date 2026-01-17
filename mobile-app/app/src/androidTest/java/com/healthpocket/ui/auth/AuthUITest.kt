package com.healthpocket.ui.auth

import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalIndirectTouchTypeApi
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
class AuthUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = {},
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("HealthPocket").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage your health daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Sign up").assertIsDisplayed()
    }

    @Test
    fun loginScreenAllowsEmailAndPasswordInput() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = {},
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("test@example.com").assertExists()
    }

    @Test
    fun loginScreenTogglesPasswordVisibility() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = {},
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun loginScreenNavigatesToRegister() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = {},
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            }
                        )
                    }
                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            viewModel = hiltViewModel(),
                            onRegisterSuccess = {},
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Don't have an account? Sign up").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create account").assertIsDisplayed()
    }

    @Test
    fun registerScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Register.route) {
                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            viewModel = hiltViewModel(),
                            onRegisterSuccess = {},
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create account").assertIsDisplayed()
        composeTestRule.onNodeWithText("First name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Sign in").assertIsDisplayed()
    }

    @Test
    fun registerScreenAllowsFormInput() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Register.route) {
                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            viewModel = hiltViewModel(),
                            onRegisterSuccess = {},
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("First name").performTextInput("John")
        composeTestRule.onNodeWithText("Last name").performTextInput("Doe")
        composeTestRule.onNodeWithText("Email").performTextInput("john@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm password").performTextInput("password123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("John").assertExists()
    }

    @Test
    fun registerScreenShowsPasswordMismatchError() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Register.route) {
                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            viewModel = hiltViewModel(),
                            onRegisterSuccess = {},
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm password").performTextInput("password456")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Passwords don't match").assertIsDisplayed()
    }

    @Test
    fun registerScreenNavigatesToLogin() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = NavRoutes.Register.route) {
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = {},
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            }
                        )
                    }
                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            viewModel = hiltViewModel(),
                            onRegisterSuccess = {},
                            onNavigateToLogin = {
                                navController.navigate(NavRoutes.Login.route)
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Already have an account? Sign in")
            .assertExists()
            .performClick();

        composeTestRule.waitForIdle()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("HealthPocket", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }
}
