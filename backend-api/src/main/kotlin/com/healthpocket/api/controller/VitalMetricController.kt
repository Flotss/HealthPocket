package com.healthpocket.api.controller

import com.healthpocket.api.dto.*
import com.healthpocket.api.model.MetricType
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.VitalMetricService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/vitals")
@Tag(name = "Vital Metrics", description = "Vital signs tracking")
@SecurityRequirement(name = "bearerAuth")
class VitalMetricController(
    private val vitalMetricService: VitalMetricService
) {

    @GetMapping
    @Operation(summary = "Get all vital metrics")
    fun getAllVitalMetrics(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<VitalMetricResponse>> {
        val metrics = vitalMetricService.getAllVitalMetrics(principal.userId)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping("/summary")
    @Operation(summary = "Get vital metrics summary (latest of each type)")
    fun getVitalMetricsSummary(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<VitalMetricsSummary> {
        val summary = vitalMetricService.getVitalMetricsSummary(principal.userId)
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get vital metrics by type")
    fun getVitalMetricsByType(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable type: MetricType
    ): ResponseEntity<List<VitalMetricResponse>> {
        val metrics = vitalMetricService.getVitalMetricsByType(principal.userId, type)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping("/range")
    @Operation(summary = "Get vital metrics in date range")
    fun getVitalMetricsInRange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: OffsetDateTime
    ): ResponseEntity<List<VitalMetricResponse>> {
        val metrics = vitalMetricService.getVitalMetricsInRange(principal.userId, startDate, endDate)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vital metric by ID")
    fun getVitalMetric(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<VitalMetricResponse> {
        val metric = vitalMetricService.getVitalMetricById(principal.userId, id)
        return ResponseEntity.ok(metric)
    }

    @PostMapping
    @Operation(summary = "Create a new vital metric")
    fun createVitalMetric(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: VitalMetricRequest
    ): ResponseEntity<VitalMetricResponse> {
        val metric = vitalMetricService.createVitalMetric(principal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(metric)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vital metric")
    fun updateVitalMetric(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: VitalMetricRequest
    ): ResponseEntity<VitalMetricResponse> {
        val metric = vitalMetricService.updateVitalMetric(principal.userId, id, request)
        return ResponseEntity.ok(metric)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vital metric")
    fun deleteVitalMetric(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        vitalMetricService.deleteVitalMetric(principal.userId, id)
        return ResponseEntity.noContent().build()
    }
}

