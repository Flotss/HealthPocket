package com.healthpocket.util

import com.healthpocket.data.local.entity.MetricType
import org.junit.Assert.assertEquals
import org.junit.Test

class MetricUtilsTest {

    @Test
    fun `getUnitForType returns kg for WEIGHT`() {
        val result = MetricUtils.getUnitForType(MetricType.WEIGHT)
        
        assertEquals("kg", result)
    }

    @Test
    fun `getUnitForType returns mmHg for BLOOD_PRESSURE`() {
        val result = MetricUtils.getUnitForType(MetricType.BLOOD_PRESSURE)
        
        assertEquals("mmHg", result)
    }

    @Test
    fun `getUnitForType returns mg per dL for BLOOD_GLUCOSE`() {
        val result = MetricUtils.getUnitForType(MetricType.BLOOD_GLUCOSE)
        
        assertEquals("mg/dL", result)
    }

    @Test
    fun `getUnitForType returns bpm for HEART_RATE`() {
        val result = MetricUtils.getUnitForType(MetricType.HEART_RATE)
        
        assertEquals("bpm", result)
    }

    @Test
    fun `getUnitForType returns degree C for TEMPERATURE`() {
        val result = MetricUtils.getUnitForType(MetricType.TEMPERATURE)
        
        assertEquals("\u00B0C", result)
    }
}
