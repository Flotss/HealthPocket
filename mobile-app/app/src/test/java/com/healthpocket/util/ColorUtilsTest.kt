package com.healthpocket.util

import com.healthpocket.ui.theme.AppColors
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun `getMedicationColorHex returns correct color for index 0`() {
        val result = ColorUtils.getMedicationColorHex(0)
        
        assertEquals(AppColors.MEDICATION_GREEN, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 1`() {
        val result = ColorUtils.getMedicationColorHex(1)
        
        assertEquals(AppColors.MEDICATION_BLUE, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 2`() {
        val result = ColorUtils.getMedicationColorHex(2)
        
        assertEquals(AppColors.MEDICATION_RED, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 3`() {
        val result = ColorUtils.getMedicationColorHex(3)
        
        assertEquals(AppColors.MEDICATION_ORANGE, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 4`() {
        val result = ColorUtils.getMedicationColorHex(4)
        
        assertEquals(AppColors.MEDICATION_PURPLE, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 5`() {
        val result = ColorUtils.getMedicationColorHex(5)
        
        assertEquals(AppColors.MEDICATION_TEAL, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 6`() {
        val result = ColorUtils.getMedicationColorHex(6)
        
        assertEquals(AppColors.MEDICATION_PINK, result)
    }

    @Test
    fun `getMedicationColorHex returns correct color for index 7`() {
        val result = ColorUtils.getMedicationColorHex(7)
        
        assertEquals(AppColors.MEDICATION_INDIGO, result)
    }

    @Test
    fun `getMedicationColorHex returns default color for index out of range`() {
        val result = ColorUtils.getMedicationColorHex(10)
        
        assertEquals(AppColors.MEDICATION_DEFAULT, result)
    }

    @Test
    fun `getMedicationColorHex returns default color for negative index`() {
        val result = ColorUtils.getMedicationColorHex(-1)
        
        assertEquals(AppColors.MEDICATION_DEFAULT, result)
    }
}
