package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.UserDao
import com.healthpocket.data.local.entity.UserEntity
import com.healthpocket.data.preferences.UserPreferences
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AuthResponse
import com.healthpocket.data.remote.dto.LoginRequest
import com.healthpocket.data.remote.dto.RegisterRequest
import com.healthpocket.data.remote.dto.UpdateProfileRequest
import com.healthpocket.data.remote.dto.UserResponse
import com.healthpocket.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication and user operations.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: HealthPocketApi,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    val isLoggedIn: Flow<Boolean> = userPreferences.isLoggedIn

    fun getCurrentUser(): Flow<UserEntity?> = userDao.getCurrentUser()

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )

            val response = api.register(request)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    saveAuthData(authResponse)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email = email, password = password)
            val response = api.login(request)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    saveAuthData(authResponse)
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            // Ignore network errors on logout
        }

        userPreferences.clearAuthData()
        userDao.deleteAll()
    }

    suspend fun refreshUserProfile(): Result<Unit> {
        // Check token expiration before making API call
        if (!checkTokenExpiration()) {
            return Result.failure(Exception("Token expired"))
        }

        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userEntity = userResponse.toEntity()
                    userDao.insert(userEntity)
                    userPreferences.setDarkMode(userResponse.darkModeEnabled)
                    Result.success(Unit)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        bloodType: String?,
        allergies: List<String>,
        emergencyContactName: String?,
        emergencyContactPhone: String?
    ): Result<UserEntity> {
        if (!checkTokenExpiration()) {
            return Result.failure(Exception("Token expired"))
        }

        val allergiesPayload = allergies.map { it.trim() }.filter { it.isNotEmpty() }

        return try {
            val request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                bloodType = bloodType,
                allergies = if (allergiesPayload.isEmpty()) emptyList() else allergiesPayload,
                emergencyContactName = emergencyContactName?.takeIf { it.isNotBlank() },
                emergencyContactPhone = emergencyContactPhone?.takeIf { it.isNotBlank() }
            )

            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userEntity = userResponse.toEntity()
                    userDao.insert(userEntity)
                    userPreferences.setDarkMode(userEntity.darkModeEnabled)
                    Result.success(userEntity)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Profile update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isAuthenticated(): Boolean {
        val token = userPreferences.accessToken.first()
        if (token.isNullOrBlank()) {
            return false
        }

        // Check if token is expired
        val expirationTimestamp = userPreferences.tokenExpirationTimestamp.first()
        if (expirationTimestamp != null && System.currentTimeMillis() >= expirationTimestamp) {
            // Token expired, logout automatically
            logout()
            return false
        }

        return true
    }

    /**
     * Check if the current token is expired and logout if necessary.
     * Returns true if token is valid, false if expired and logged out.
     */
    suspend fun checkTokenExpiration(): Boolean {
        val expirationTimestamp = userPreferences.tokenExpirationTimestamp.first()
        if (expirationTimestamp != null && System.currentTimeMillis() >= expirationTimestamp) {
            // Token expired, logout automatically
            logout()
            return false
        }
        return true
    }

    private suspend fun saveAuthData(authResponse: AuthResponse) {
        userPreferences.saveAuthData(
            accessToken = authResponse.accessToken,
            refreshToken = authResponse.refreshToken,
            userId = authResponse.user.id,
            email = authResponse.user.email,
            expiresIn = authResponse.expiresIn
        )

        // Save user to local DB
        val userEntity = authResponse.user.toEntity()
        userDao.insert(userEntity)
    }

    private fun UserResponse.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate?.let { DateTimeUtils.parseDateString(it) },
            gender = gender,
            bloodType = bloodType,
            allergies = allergies.joinToString(","),
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            preferredLanguage = preferredLanguage,
            darkModeEnabled = darkModeEnabled
        )
    }
}
