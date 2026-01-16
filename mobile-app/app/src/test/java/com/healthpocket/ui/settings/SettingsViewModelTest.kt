package com.healthpocket.ui.settings

import com.healthpocket.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var userPreferences: UserPreferences
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = mock()
        whenever(userPreferences.darkMode).thenReturn(flowOf(false))
        whenever(userPreferences.language).thenReturn(flowOf("fr"))
        whenever(userPreferences.isLoggedIn).thenReturn(flowOf(true))
        viewModel = SettingsViewModel(userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setDarkMode calls userPreferences setDarkMode`() = runTest {
        whenever(userPreferences.setDarkMode(any())).thenReturn(Unit)

        viewModel.setDarkMode(true)

        verify(userPreferences).setDarkMode(true)
    }

    @Test
    fun `setLanguage calls userPreferences setLanguage`() = runTest {
        whenever(userPreferences.setLanguage(any())).thenReturn(Unit)

        viewModel.setLanguage("en")

        verify(userPreferences).setLanguage("en")
    }
}
