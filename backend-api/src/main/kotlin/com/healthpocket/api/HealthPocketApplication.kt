package com.healthpocket.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main entry point for the HealthPocket API application.
 * 
 * This Spring Boot application provides a REST API for:
 * - User authentication and profile management
 * - Medication tracking and reminders
 * - Health appointments scheduling
 * - Daily health journal entries
 * - Vital metrics monitoring
 * - Offline-first data synchronization
 */
@SpringBootApplication
class HealthPocketApplication

fun main(args: Array<String>) {
    runApplication<HealthPocketApplication>(*args)
}

