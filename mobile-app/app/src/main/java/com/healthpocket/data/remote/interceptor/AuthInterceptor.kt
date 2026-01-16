package com.healthpocket.data.remote.interceptor

import android.util.Log
import com.healthpocket.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that adds the JWT token to authenticated requests.
 * Also checks token expiration and triggers logout if expired.
 */
class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for public endpoints
        if (originalRequest.url.encodedPath.contains("/api/auth/")) {
            return chain.proceed(originalRequest)
        }

        // Check token expiration synchronously (blocking is acceptable in interceptors)
        val expirationTimestamp = runBlocking { userPreferences.tokenExpirationTimestamp.first() }
        val currentTime = System.currentTimeMillis()

        if (expirationTimestamp != null && currentTime >= expirationTimestamp) {
            // Token expired, logout automatically by clearing auth data
            Log.w("AuthInterceptor", "Token expired, logging out user")
            runBlocking {
                userPreferences.clearAuthData()
            }
            return chain.proceed(originalRequest)
        }

        // Get token synchronously (blocking is acceptable in interceptors)
        val token = runBlocking { userPreferences.accessToken.first() }

        val authenticatedRequest = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        // Proceed with the request
        val response = chain.proceed(authenticatedRequest)

        // Check response status - if 401 (Unauthorized) or 403 (Forbidden), token is invalid
        if (response.code == 401 || response.code == 403) {
            Log.w(
                "AuthInterceptor",
                "Received ${response.code} response, token invalid. Logging out user"
            )
            // Clear auth data asynchronously (don't block the response)
            runBlocking {
                userPreferences.clearAuthData()
            }
        }

        return response
    }
}

