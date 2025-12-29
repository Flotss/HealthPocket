package com.healthpocket.api.service

import com.healthpocket.api.dto.ChangePasswordRequest
import com.healthpocket.api.dto.UpdateProfileRequest
import com.healthpocket.api.dto.UserResponse
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.User
import com.healthpocket.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findById(userId: UUID): User {
        return userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun getProfile(userId: UUID): UserResponse {
        val user = findById(userId)
        return UserResponse.fromEntity(user)
    }

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse {
        val user = findById(userId)

        user.firstName = request.firstName
        user.lastName = request.lastName
        request.birthDate?.let { user.birthDate = it }
        request.gender?.let { user.gender = it }
        request.bloodType?.let { user.bloodType = it }
        request.allergies?.let { user.allergies = toJsonArray(it) }
        request.emergencyContactName?.let { user.emergencyContactName = it }
        request.emergencyContactPhone?.let { user.emergencyContactPhone = it }
        request.preferredLanguage?.let { user.preferredLanguage = it }
        request.darkModeEnabled?.let { user.darkModeEnabled = it }

        val savedUser = userRepository.save(user)
        return UserResponse.fromEntity(savedUser)
    }

    @Transactional
    fun changePassword(userId: UUID, request: ChangePasswordRequest): Boolean {
        val user = findById(userId)

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        user.passwordHash = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
        return true
    }

    private fun toJsonArray(items: List<String>): String {
        return items.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
    }
}

