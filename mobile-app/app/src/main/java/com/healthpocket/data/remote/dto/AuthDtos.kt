package com.healthpocket.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request DTOs for authentication.
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val email: String,
    val password: String,
    @field:Json(name = "firstName") val firstName: String,
    @field:Json(name = "lastName") val lastName: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @field:Json(name = "refreshToken") val refreshToken: String
)

/**
 * Response DTOs for authentication.
 */
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @field:Json(name = "accessToken") val accessToken: String,
    @field:Json(name = "refreshToken") val refreshToken: String,
    @field:Json(name = "tokenType") val tokenType: String,
    @field:Json(name = "expiresIn") val expiresIn: Long,
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @field:Json(name = "accessToken") val accessToken: String,
    @field:Json(name = "refreshToken") val refreshToken: String,
    @field:Json(name = "tokenType") val tokenType: String,
    @field:Json(name = "expiresIn") val expiresIn: Long
)

