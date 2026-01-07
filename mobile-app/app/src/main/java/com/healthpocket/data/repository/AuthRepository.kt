package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.UserDao
import com.healthpocket.data.local.entity.UserEntity
import com.healthpocket.data.preferences.UserPreferences
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AuthResponse
import com.healthpocket.data.remote.dto.LoginRequest
import com.healthpocket.data.remote.dto.RegisterRequest
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
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userEntity = UserEntity(
                        id = userResponse.id,
                        email = userResponse.email,
                        firstName = userResponse.firstName,
                        lastName = userResponse.lastName,
                        birthDate = userResponse.birthDate?.let { 
                            com.healthpocket.util.DateTimeUtils.parseDateString(it) 
                        },
                        gender = userResponse.gender,
                        bloodType = userResponse.bloodType,
                        allergies = userResponse.allergies.joinToString(","),
                        emergencyContactName = userResponse.emergencyContactName,
                        emergencyContactPhone = userResponse.emergencyContactPhone,
                        preferredLanguage = userResponse.preferredLanguage,
                        darkModeEnabled = userResponse.darkModeEnabled
                    )
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

    suspend fun isAuthenticated(): Boolean {
        return userPreferences.accessToken.first()?.isNotBlank() == true
    }

    private suspend fun saveAuthData(authResponse: AuthResponse) {
        userPreferences.saveAuthData(
            accessToken = authResponse.accessToken,
            refreshToken = authResponse.refreshToken,
            userId = authResponse.user.id,
            email = authResponse.user.email
        )

        // Save user to local DB
        val userEntity = UserEntity(
            id = authResponse.user.id,
            email = authResponse.user.email,
            firstName = authResponse.user.firstName,
            lastName = authResponse.user.lastName,
            birthDate = authResponse.user.birthDate?.let { 
                com.healthpocket.util.DateTimeUtils.parseDateString(it) 
            },
            gender = authResponse.user.gender,
            bloodType = authResponse.user.bloodType,
            allergies = authResponse.user.allergies.joinToString(","),
            emergencyContactName = authResponse.user.emergencyContactName,
            emergencyContactPhone = authResponse.user.emergencyContactPhone,
            preferredLanguage = authResponse.user.preferredLanguage,
            darkModeEnabled = authResponse.user.darkModeEnabled
        )
        userDao.insert(userEntity)
    }
}

