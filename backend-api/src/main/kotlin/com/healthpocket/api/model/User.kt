package com.healthpocket.api.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * User entity representing a registered user in the system.
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @Column(name = "blood_type")
    var bloodType: String? = null,

    @Column(columnDefinition = "TEXT")
    var allergies: String? = null, // JSON array stored as string

    @Column(name = "emergency_contact_name")
    var emergencyContactName: String? = null,

    @Column(name = "emergency_contact_phone")
    var emergencyContactPhone: String? = null,

    @Column(name = "preferred_language")
    var preferredLanguage: String = "fr",

    @Column(name = "dark_mode_enabled")
    var darkModeEnabled: Boolean = false,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

