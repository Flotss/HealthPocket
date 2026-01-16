package com.healthpocket.api.service

import com.healthpocket.api.dto.ChangePasswordRequest
import com.healthpocket.api.dto.UpdateProfileRequest
import com.healthpocket.api.exception.ResourceNotFoundException
import com.healthpocket.api.model.Gender
import com.healthpocket.api.model.User
import com.healthpocket.api.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserService

    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `getProfile should return user profile`() {
        // Given
        val user = createUser()

        every { userRepository.findById(userId) } returns Optional.of(user)

        // When
        val result = userService.getProfile(userId)

        // Then
        assertNotNull(result)
        assertEquals(user.email, result.email)
        assertEquals(user.firstName, result.firstName)
        assertEquals(user.lastName, result.lastName)
    }

    @Test
    fun `getProfile should throw exception when user not found`() {
        // Given
        every { userRepository.findById(userId) } returns Optional.empty()

        // When/Then
        assertThrows<ResourceNotFoundException> {
            userService.getProfile(userId)
        }
    }

    @Test
    fun `updateProfile should update user profile`() {
        // Given
        val user = createUser()
        val request = UpdateProfileRequest(
            firstName = "Updated",
            lastName = "Name",
            birthDate = null,
            gender = Gender.MALE,
            bloodType = "O+",
            allergies = listOf("Peanuts"),
            emergencyContactName = "Emergency Contact",
            emergencyContactPhone = "1234567890",
            preferredLanguage = "en",
            darkModeEnabled = true
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user

        // When
        val result = userService.updateProfile(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Updated", result.firstName)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `changePassword should update password when current password is correct`() {
        // Given
        val user = createUser()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.matches(request.currentPassword, user.passwordHash) } returns true
        every { passwordEncoder.encode(request.newPassword) } returns "newHashedPassword"
        every { userRepository.save(any()) } returns user

        // When
        val result = userService.changePassword(userId, request)

        // Then
        assertTrue(result)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `changePassword should throw exception when current password is incorrect`() {
        // Given
        val user = createUser()
        val request = ChangePasswordRequest(
            currentPassword = "wrongPassword",
            newPassword = "newPassword"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.matches(request.currentPassword, user.passwordHash) } returns false

        // When/Then
        assertThrows<IllegalArgumentException> {
            userService.changePassword(userId, request)
        }
    }

    private fun createUser(): User {
        return User(
            id = userId,
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
    }
}
