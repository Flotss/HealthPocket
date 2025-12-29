package com.healthpocket.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for settings and preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isDarkMode: Flow<Boolean> = userPreferences.darkMode
    val language: Flow<String> = userPreferences.language
    val isLoggedIn: Flow<Boolean> = userPreferences.isLoggedIn

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(lang)
        }
    }
}

