package com.healthpocket.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * DataStore-based preferences for user settings and authentication tokens.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    // Keys
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val TOKEN_EXPIRATION_TIMESTAMP = longPreferencesKey("token_expiration_timestamp")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // Access Token
    val accessToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN] }

    // Refresh Token
    val refreshToken: Flow<String?> = dataStore.data.map { it[REFRESH_TOKEN] }

    // User ID
    val userId: Flow<String?> = dataStore.data.map { it[USER_ID] }

    // User Email
    val userEmail: Flow<String?> = dataStore.data.map { it[USER_EMAIL] }

    // Token Expiration Timestamp
    val tokenExpirationTimestamp: Flow<Long?> = dataStore.data.map { it[TOKEN_EXPIRATION_TIMESTAMP] }

    // Dark Mode
    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    // Language
    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: "fr" }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE] = lang
        }
    }

    // Is Logged In
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN] ?: false }

    /**
     * Save authentication data after login.
     */
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        email: String,
        expiresIn: Long // expiration time in seconds
    ) {
        val expirationTimestamp = System.currentTimeMillis() + (expiresIn * 1000)
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            prefs[USER_ID] = userId
            prefs[USER_EMAIL] = email
            prefs[TOKEN_EXPIRATION_TIMESTAMP] = expirationTimestamp
            prefs[IS_LOGGED_IN] = true
        }
    }

    /**
     * Clear all authentication data on logout.
     */
    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(USER_ID)
            prefs.remove(USER_EMAIL)
            prefs.remove(TOKEN_EXPIRATION_TIMESTAMP)
            prefs[IS_LOGGED_IN] = false
        }
    }
}

