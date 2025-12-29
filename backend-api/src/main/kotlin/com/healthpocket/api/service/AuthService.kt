package com.healthpocket.api.service

import com.healthpocket.api.dto.*
import com.healthpocket.api.exception.BadRequestException
import com.healthpocket.api.exception.UnauthorizedException
import com.healthpocket.api.model.RefreshToken
import com.healthpocket.api.model.User
import com.healthpocket.api.repository.RefreshTokenRepository
import com.healthpocket.api.repository.UserRepository
import com.healthpocket.api.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email already registered")
        }

        // Create new user
        val user = User(
            email = request.email.lowercase().trim(),
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim()
        )

        val savedUser = userRepository.save(user)
        return generateAuthResponse(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email.lowercase().trim())
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw UnauthorizedException("Invalid refresh token")

        if (storedToken.expiresAt.isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(storedToken)
            throw UnauthorizedException("Refresh token expired")
        }

        val user = userRepository.findById(storedToken.userId)
            .orElseThrow { UnauthorizedException("User not found") }

        // Delete old refresh token
        refreshTokenRepository.delete(storedToken)

        // Generate new tokens
        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken()

        // Save new refresh token
        val refreshTokenEntity = RefreshToken(
            userId = user.id!!,
            token = newRefreshToken,
            expiresAt = OffsetDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000
        )
    }

    @Transactional
    fun logout(userId: UUID) {
        refreshTokenRepository.deleteByUserId(userId)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
        val refreshToken = jwtTokenProvider.generateRefreshToken()

        // Save refresh token
        val refreshTokenEntity = RefreshToken(
            userId = user.id!!,
            token = refreshToken,
            expiresAt = OffsetDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000,
            user = UserResponse.fromEntity(user)
        )
    }
}

