package com.healthpocket.util

import com.healthpocket.ui.theme.AppColors

/**
 * Utility functions for color operations.
 */
object ColorUtils {

    /**
     * Get medication color hex string by index from MedicationColors list.
     */
    fun getMedicationColorHex(index: Int): String {
        return when (index) {
            0 -> AppColors.MEDICATION_GREEN
            1 -> AppColors.MEDICATION_BLUE
            2 -> AppColors.MEDICATION_RED
            3 -> AppColors.MEDICATION_ORANGE
            4 -> AppColors.MEDICATION_PURPLE
            5 -> AppColors.MEDICATION_TEAL
            6 -> AppColors.MEDICATION_PINK
            7 -> AppColors.MEDICATION_INDIGO
            else -> AppColors.MEDICATION_DEFAULT
        }
    }
}
