package com.healthpocket.api.service

import com.healthpocket.api.dto.LoginRequest
import com.healthpocket.api.dto.RegisterRequest
import com.healthpocket.api.exception.BadRequestException
import com.healthpocket.api.exception.UnauthorizedException
import com.healthpocket.api.model.User
import com.healthpocket.api.repository.RefreshTokenRepository
import com.healthpocket.api.repository.UserRepository
import com.healthpocket.api.security.JwtTokenProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            userRepository,
            refreshTokenRepository,
            passwordEncoder,
            jwtTokenProvider
        )
    }

    @Test
    fun `register should create new user and return auth response`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        val userId = UUID.randomUUID()
        val savedUser = User(
            id = userId,
            email = request.email,
            passwordHash = "hashedPassword",
            firstName = request.firstName,
            lastName = request.lastName
        )

        every { userRepository.existsByEmail(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "hashedPassword"
        every { userRepository.save(any()) } returns savedUser
        every { jwtTokenProvider.generateAccessToken(any(), any()) } returns "accessToken"
        every { jwtTokenProvider.generateRefreshToken() } returns "refreshToken"
        every { jwtTokenProvider.getAccessTokenExpiration() } returns 86400000L
        every { refreshTokenRepository.save(any()) } returns mockk()

        // When
        val result = authService.register(request)

        // Then
        assertNotNull(result)
        assertEquals("accessToken", result.accessToken)
        assertEquals("refreshToken", result.refreshToken)
        assertEquals(request.email, result.user.email)

        verify { userRepository.existsByEmail(request.email.lowercase()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `register should throw exception when email already exists`() {
        // Given
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        every { userRepository.existsByEmail(any()) } returns true

        // When/Then
        assertThrows<BadRequestException> {
            authService.register(request)
        }
    }

    @Test
    fun `login should return auth response for valid credentials`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = request.email,
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        every { userRepository.findByEmail(any()) } returns user
        every { passwordEncoder.matches(request.password, user.passwordHash) } returns true
        every { jwtTokenProvider.generateAccessToken(any(), any()) } returns "accessToken"
        every { jwtTokenProvider.generateRefreshToken() } returns "refreshToken"
        every { jwtTokenProvider.getAccessTokenExpiration() } returns 86400000L
        every { refreshTokenRepository.save(any()) } returns mockk()

        // When
        val result = authService.login(request)

        // Then
        assertNotNull(result)
        assertEquals("accessToken", result.accessToken)
        assertEquals(request.email, result.user.email)
    }

    @Test
    fun `login should throw exception for invalid email`() {
        // Given
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        )

        every { userRepository.findByEmail(any()) } returns null

        // When/Then
        assertThrows<UnauthorizedException> {
            authService.login(request)
        }
    }

    @Test
    fun `login should throw exception for invalid password`() {
        // Given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        val user = User(
            id = UUID.randomUUID(),
            email = request.email,
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        every { userRepository.findByEmail(any()) } returns user
        every { passwordEncoder.matches(request.password, user.passwordHash) } returns false

        // When/Then
        assertThrows<UnauthorizedException> {
            authService.login(request)
        }
    }
}

