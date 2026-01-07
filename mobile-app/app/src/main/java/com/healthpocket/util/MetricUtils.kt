package com.healthpocket.util

import com.healthpocket.data.local.entity.MetricType

/**
 * Utility functions for health metrics.
 */
object MetricUtils {

    /**
     * Get the appropriate unit for a metric type.
     */
    fun getUnitForType(type: MetricType): String {
        return when (type) {
            MetricType.WEIGHT -> "kg"
            MetricType.BLOOD_PRESSURE -> "mmHg"
            MetricType.BLOOD_GLUCOSE -> "mg/dL"
            MetricType.HEART_RATE -> "bpm"
            MetricType.TEMPERATURE -> "°C"
        }
    }
}
