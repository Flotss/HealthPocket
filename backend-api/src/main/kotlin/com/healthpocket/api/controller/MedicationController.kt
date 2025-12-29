package com.healthpocket.api.controller

import com.healthpocket.api.dto.*
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.MedicationService
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
@RequestMapping("/api/medications")
@Tag(name = "Medications", description = "Medication tracking")
@SecurityRequirement(name = "bearerAuth")
class MedicationController(
    private val medicationService: MedicationService
) {

    @GetMapping
    @Operation(summary = "Get all medications")
    fun getAllMedications(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<MedicationResponse>> {
        val medications = medicationService.getAllMedications(principal.userId)
        return ResponseEntity.ok(medications)
    }

    @GetMapping("/active")
    @Operation(summary = "Get active medications")
    fun getActiveMedications(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<MedicationResponse>> {
        val medications = medicationService.getActiveMedications(principal.userId)
        return ResponseEntity.ok(medications)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medication by ID")
    fun getMedication(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<MedicationResponse> {
        val medication = medicationService.getMedicationById(principal.userId, id)
        return ResponseEntity.ok(medication)
    }

    @PostMapping
    @Operation(summary = "Create a new medication")
    fun createMedication(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: MedicationRequest
    ): ResponseEntity<MedicationResponse> {
        val medication = medicationService.createMedication(principal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(medication)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a medication")
    fun updateMedication(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: MedicationRequest
    ): ResponseEntity<MedicationResponse> {
        val medication = medicationService.updateMedication(principal.userId, id, request)
        return ResponseEntity.ok(medication)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a medication")
    fun deleteMedication(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        medicationService.deleteMedication(principal.userId, id)
        return ResponseEntity.noContent().build()
    }

    // Medication Intakes

    @GetMapping("/{medicationId}/intakes")
    @Operation(summary = "Get intakes for a medication")
    fun getIntakesForMedication(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable medicationId: UUID
    ): ResponseEntity<List<MedicationIntakeResponse>> {
        val intakes = medicationService.getIntakesForMedication(principal.userId, medicationId)
        return ResponseEntity.ok(intakes)
    }

    @GetMapping("/intakes")
    @Operation(summary = "Get intakes in date range")
    fun getIntakesInRange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: OffsetDateTime
    ): ResponseEntity<List<MedicationIntakeResponse>> {
        val intakes = medicationService.getIntakesInRange(principal.userId, startDate, endDate)
        return ResponseEntity.ok(intakes)
    }

    @PostMapping("/intakes")
    @Operation(summary = "Create a medication intake")
    fun createIntake(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: MedicationIntakeRequest
    ): ResponseEntity<MedicationIntakeResponse> {
        val intake = medicationService.createIntake(principal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(intake)
    }

    @PutMapping("/intakes/{id}/status")
    @Operation(summary = "Update intake status")
    fun updateIntakeStatus(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateIntakeStatusRequest
    ): ResponseEntity<MedicationIntakeResponse> {
        val intake = medicationService.updateIntakeStatus(principal.userId, id, request)
        return ResponseEntity.ok(intake)
    }
}

