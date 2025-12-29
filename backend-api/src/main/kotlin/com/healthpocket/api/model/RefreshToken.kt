package com.healthpocket.api.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

/**
 * RefreshToken entity for JWT token refresh functionality.
 */
@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false, unique = true, length = 500)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: OffsetDateTime,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

