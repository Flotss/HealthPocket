package com.healthpocket.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Helper function to convert hex color string to Compose Color.
 */
private fun String.toColor(): Color {
    return Color(this.toColorInt())
}

// Primary colors - Teal/Cyan health theme
val Primary = AppColors.PRIMARY.toColor()
val PrimaryLight = AppColors.PRIMARY_LIGHT.toColor()
val PrimaryDark = AppColors.PRIMARY_DARK.toColor()
val OnPrimary = Color.White

// Secondary colors - Warm coral accent
val Secondary = AppColors.SECONDARY.toColor()
val SecondaryLight = AppColors.SECONDARY_LIGHT.toColor()
val SecondaryDark = AppColors.SECONDARY_DARK.toColor()
val OnSecondary = Color.White

// Tertiary - Purple for special elements
val Tertiary = AppColors.TERTIARY.toColor()
val TertiaryLight = AppColors.TERTIARY_LIGHT.toColor()
val OnTertiary = Color.White

// Background colors
val BackgroundLight = AppColors.BACKGROUND_LIGHT.toColor()
val BackgroundDark = AppColors.BACKGROUND_DARK.toColor()
val SurfaceLight = AppColors.SURFACE_LIGHT.toColor()
val SurfaceDark = AppColors.SURFACE_DARK.toColor()

// Status colors
val Success = AppColors.SUCCESS.toColor()
val Warning = AppColors.WARNING.toColor()
val Error = AppColors.ERROR.toColor()
val Info = AppColors.INFO.toColor()

// Text colors
val OnBackgroundLight = AppColors.TEXT_LIGHT.toColor()
val OnBackgroundDark = AppColors.TEXT_DARK.toColor()
val OnSurfaceLight = AppColors.TEXT_LIGHT.toColor()
val OnSurfaceDark = AppColors.TEXT_DARK.toColor()

// Mood colors
val MoodExcellent = AppColors.MOOD_EXCELLENT.toColor()
val MoodGood = AppColors.MOOD_GOOD.toColor()
val MoodNeutral = AppColors.MOOD_NEUTRAL.toColor()
val MoodBad = AppColors.MOOD_BAD.toColor()
val MoodVeryBad = AppColors.MOOD_VERY_BAD.toColor()

// Medication colors
val MedicationColors = listOf(
    AppColors.MEDICATION_GREEN.toColor(),
    AppColors.MEDICATION_BLUE.toColor(),
    AppColors.MEDICATION_RED.toColor(),
    AppColors.MEDICATION_ORANGE.toColor(),
    AppColors.MEDICATION_PURPLE.toColor(),
    AppColors.MEDICATION_TEAL.toColor(),
    AppColors.MEDICATION_PINK.toColor(),
    AppColors.MEDICATION_INDIGO.toColor()
)
