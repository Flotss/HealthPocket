package com.healthpocket.api.controller

import com.healthpocket.api.dto.*
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.AppointmentService
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
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Medical appointments management")
@SecurityRequirement(name = "bearerAuth")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @GetMapping
    @Operation(summary = "Get all appointments")
    fun getAllAppointments(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<AppointmentResponse>> {
        val appointments = appointmentService.getAllAppointments(principal.userId)
        return ResponseEntity.ok(appointments)
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming appointments")
    fun getUpcomingAppointments(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<AppointmentResponse>> {
        val appointments = appointmentService.getUpcomingAppointments(principal.userId)
        return ResponseEntity.ok(appointments)
    }

    @GetMapping("/range")
    @Operation(summary = "Get appointments in date range")
    fun getAppointmentsInRange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: OffsetDateTime
    ): ResponseEntity<List<AppointmentResponse>> {
        val appointments = appointmentService.getAppointmentsInRange(principal.userId, startDate, endDate)
        return ResponseEntity.ok(appointments)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID")
    fun getAppointment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<AppointmentResponse> {
        val appointment = appointmentService.getAppointmentById(principal.userId, id)
        return ResponseEntity.ok(appointment)
    }

    @PostMapping
    @Operation(summary = "Create a new appointment")
    fun createAppointment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: AppointmentRequest
    ): ResponseEntity<AppointmentResponse> {
        val appointment = appointmentService.createAppointment(principal.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an appointment")
    fun updateAppointment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AppointmentRequest
    ): ResponseEntity<AppointmentResponse> {
        val appointment = appointmentService.updateAppointment(principal.userId, id, request)
        return ResponseEntity.ok(appointment)
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update appointment status")
    fun updateAppointmentStatus(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAppointmentStatusRequest
    ): ResponseEntity<AppointmentResponse> {
        val appointment = appointmentService.updateAppointmentStatus(principal.userId, id, request)
        return ResponseEntity.ok(appointment)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an appointment")
    fun deleteAppointment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        appointmentService.deleteAppointment(principal.userId, id)
        return ResponseEntity.noContent().build()
    }
}

