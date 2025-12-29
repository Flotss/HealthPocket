package com.healthpocket.api.repository

import com.healthpocket.api.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): RefreshToken?
    fun findByUserId(userId: UUID): List<RefreshToken>
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
    fun deleteByUserId(userId: UUID)
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    fun deleteExpiredTokens(now: OffsetDateTime)
}

