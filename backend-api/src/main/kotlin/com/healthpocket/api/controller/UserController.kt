package com.healthpocket.api.controller

import com.healthpocket.api.dto.*
import com.healthpocket.api.security.UserPrincipal
import com.healthpocket.api.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    fun getProfile(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        val profile = userService.getProfile(principal.userId)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val profile = userService.updateProfile(principal.userId, request)
        return ResponseEntity.ok(profile)
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    fun changePassword(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        userService.changePassword(principal.userId, request)
        return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
    }
}

