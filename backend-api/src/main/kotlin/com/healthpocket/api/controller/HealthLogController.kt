package com.healthpocket.api.controller

import com.healthpocket.api.dto.*
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.HealthLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/health-logs")
@Tag(name = "Health Logs", description = "Daily health journal")
@SecurityRequirement(name = "bearerAuth")
class HealthLogController(
    private val healthLogService: HealthLogService
) {

    @GetMapping
    @Operation(summary = "Get all health logs")
    fun getAllHealthLogs(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<HealthLogResponse>> {
        val logs = healthLogService.getAllHealthLogs(principal.userId)
        return ResponseEntity.ok(logs)
    }

    @GetMapping("/range")
    @Operation(summary = "Get health logs in date range")
    fun getHealthLogsInRange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<HealthLogResponse>> {
        val logs = healthLogService.getHealthLogsInRange(principal.userId, startDate, endDate)
        return ResponseEntity.ok(logs)
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get health log for a specific date")
    fun getHealthLogByDate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<HealthLogResponse> {
        val log = healthLogService.getHealthLogByDate(principal.userId, date)
        return if (log != null) {
            ResponseEntity.ok(log)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get health log by ID")
    fun getHealthLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<HealthLogResponse> {
        val log = healthLogService.getHealthLogById(principal.userId, id)
        return ResponseEntity.ok(log)
    }

    @PostMapping
    @Operation(summary = "Create or update health log for a date")
    fun createOrUpdateHealthLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: HealthLogRequest
    ): ResponseEntity<HealthLogResponse> {
        val log = healthLogService.createOrUpdateHealthLog(principal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(log)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a health log")
    fun updateHealthLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: HealthLogRequest
    ): ResponseEntity<HealthLogResponse> {
        val log = healthLogService.updateHealthLog(principal.userId, id, request)
        return ResponseEntity.ok(log)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a health log")
    fun deleteHealthLog(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        healthLogService.deleteHealthLog(principal.userId, id)
        return ResponseEntity.noContent().build()
    }
}

