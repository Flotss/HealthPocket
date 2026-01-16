package com.healthpocket.ui.vitals

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthpocket.MainActivity
import com.healthpocket.data.local.entity.MetricType
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
class VitalsUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun addVitalMetricScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AddVitalMetric.createRoute(MetricType.WEIGHT.name)
                ) {
                    composable(NavRoutes.AddVitalMetric.route) { backStackEntry ->
                        val typeArg = backStackEntry.arguments?.getString("type")
                        val metricType = typeArg?.let {
                            runCatching { MetricType.valueOf(it) }.getOrDefault(MetricType.WEIGHT)
                        } ?: MetricType.WEIGHT

                        AddVitalMetricScreen(
                            initialType = metricType,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Log vital metric", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Track your most recent health measurements.", useUnmergedTree = true).assertExists()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Save metric", useUnmergedTree = true).assertExists()
    }

    @Test
    fun addVitalMetricScreenAllowsWeightInput() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AddVitalMetric.createRoute(MetricType.WEIGHT.name)
                ) {
                    composable(NavRoutes.AddVitalMetric.route) { backStackEntry ->
                        val typeArg = backStackEntry.arguments?.getString("type")
                        val metricType = typeArg?.let {
                            runCatching { MetricType.valueOf(it) }.getOrDefault(MetricType.WEIGHT)
                        } ?: MetricType.WEIGHT

                        AddVitalMetricScreen(
                            initialType = metricType,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Weight").assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Value (kg)", substring = true))
            .onFirst()
            .performTextInput("75")

        composeTestRule.waitForIdle()
    }

    @Test
    fun addVitalMetricScreenAllowsBloodPressureInput() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AddVitalMetric.createRoute(MetricType.BLOOD_PRESSURE.name)
                ) {
                    composable(NavRoutes.AddVitalMetric.route) { backStackEntry ->
                        val typeArg = backStackEntry.arguments?.getString("type")
                        val metricType = typeArg?.let {
                            runCatching { MetricType.valueOf(it) }.getOrDefault(MetricType.BLOOD_PRESSURE)
                        } ?: MetricType.BLOOD_PRESSURE

                        AddVitalMetricScreen(
                            initialType = metricType,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Blood pressure").assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Value (mmHg)", substring = true))
            .onFirst()
            .performTextInput("120")
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasText("Diastolic (mmHg)", substring = true))
            .onFirst()
            .performTextInput("80")

        composeTestRule.waitForIdle()
    }

    @Test
    fun addVitalMetricScreenAllowsNotesInput() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.AddVitalMetric.createRoute(MetricType.WEIGHT.name)
                ) {
                    composable(NavRoutes.AddVitalMetric.route) { backStackEntry ->
                        val typeArg = backStackEntry.arguments?.getString("type")
                        val metricType = typeArg?.let {
                            runCatching { MetricType.valueOf(it) }.getOrDefault(MetricType.WEIGHT)
                        } ?: MetricType.WEIGHT

                        AddVitalMetricScreen(
                            initialType = metricType,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Weight").assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Notes"))
            .onFirst()
            .performTextInput("Mesuré le matin à jeun")
        composeTestRule.waitForIdle()
    }

    @Test
    fun vitalsHistoryScreenDisplaysAllElements() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.VitalsHistory.route
                ) {
                    composable(NavRoutes.VitalsHistory.route) {
                        VitalMetricsHistoryScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasText("Vitals history"))
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun vitalsHistoryScreenShowsEmptyState() {
        composeTestRule.activity.setContent {
            HealthPocketTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.VitalsHistory.route
                ) {
                    composable(NavRoutes.VitalsHistory.route) {
                        VitalMetricsHistoryScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasText("Vitals history"))
            .onFirst()
            .assertIsDisplayed()
    }
}
