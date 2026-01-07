package com.healthpocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthpocket.ui.HealthPocketApp
import com.healthpocket.ui.settings.SettingsViewModel
import com.healthpocket.ui.theme.HealthPocketTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the HealthPocket application.
 * Uses Jetpack Compose for the UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState(initial = false)
            
            HealthPocketTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HealthPocketApp()
                }
            }
        }
    }
}

