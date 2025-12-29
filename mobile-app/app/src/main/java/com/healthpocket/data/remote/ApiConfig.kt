package com.healthpocket.data.remote

/**
 * API configuration for the HealthPocket backend.
 */
object ApiConfig {
    // For Android Emulator use 10.0.2.2 to access localhost
    // For physical device, use your computer's IP address
    const val BASE_URL = "http://localhost:8080/"
    
    // Timeouts in seconds
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}

