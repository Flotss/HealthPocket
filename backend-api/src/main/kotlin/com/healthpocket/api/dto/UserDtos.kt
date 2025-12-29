package com.healthpocket.api.dto

import com.healthpocket.api.model.Gender
import com.healthpocket.api.model.User
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.util.UUID

/**
 * User response DTO.
 */
data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate?,
    val gender: Gender?,
    val bloodType: String?,
    val allergies: List<String>,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val preferredLanguage: String,
    val darkModeEnabled: Boolean
) {
    companion object {
        fun fromEntity(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                birthDate = user.birthDate,
                gender = user.gender,
                bloodType = user.bloodType,
                allergies = parseJsonArray(user.allergies),
                emergencyContactName = user.emergencyContactName,
                emergencyContactPhone = user.emergencyContactPhone,
                preferredLanguage = user.preferredLanguage,
                darkModeEnabled = user.darkModeEnabled
            )
        }

        private fun parseJsonArray(json: String?): List<String> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * Update user profile request DTO.
 */
data class UpdateProfileRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    val birthDate: LocalDate?,
    val gender: Gender?,
    val bloodType: String?,
    val allergies: List<String>?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val preferredLanguage: String?,
    val darkModeEnabled: Boolean?
)

/**
 * Change password request DTO.
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    val newPassword: String
)

