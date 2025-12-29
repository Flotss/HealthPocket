package com.healthpocket.api.controller

import com.healthpocket.api.dto.SyncRequest
import com.healthpocket.api.dto.SyncResponse
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.SyncService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync", description = "Data synchronization")
@SecurityRequirement(name = "bearerAuth")
class SyncController(
    private val syncService: SyncService
) {

    @PostMapping
    @Operation(summary = "Synchronize data between mobile and server")
    fun sync(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SyncRequest
    ): ResponseEntity<SyncResponse> {
        val response = syncService.sync(principal.userId, request)
        return ResponseEntity.ok(response)
    }
}

