package com.healthpocket.api.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(
            jwtSecret = "testSecretKeyThatIsLongEnoughForHS256Algorithm",
            accessTokenExpiration = 86400000L, // 24 hours
            refreshTokenExpiration = 604800000L // 7 days
        )
    }

    @Test
    fun `generateAccessToken should create valid token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        // When
        val token = jwtTokenProvider.generateAccessToken(userId, email)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateRefreshToken should create valid token`() {
        // When
        val token = jwtTokenProvider.generateRefreshToken()

        // Then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val token = jwtTokenProvider.generateAccessToken(userId, email)

        // When
        val isValid = jwtTokenProvider.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"

        // When
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `getUserIdFromToken should extract userId from token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val token = jwtTokenProvider.generateAccessToken(userId, email)

        // When
        val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `getEmailFromToken should extract email from token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val token = jwtTokenProvider.generateAccessToken(userId, email)

        // When
        val extractedEmail = jwtTokenProvider.getEmailFromToken(token)

        // Then
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `getAccessTokenExpiration should return correct expiration`() {
        // When
        val expiration = jwtTokenProvider.getAccessTokenExpiration()

        // Then
        assertEquals(86400000L, expiration)
    }

    @Test
    fun `getRefreshTokenExpiration should return correct expiration`() {
        // When
        val expiration = jwtTokenProvider.getRefreshTokenExpiration()

        // Then
        assertEquals(604800000L, expiration)
    }
}
