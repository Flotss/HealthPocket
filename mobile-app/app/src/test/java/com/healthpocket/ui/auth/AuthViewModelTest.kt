package com.healthpocket.ui.auth

import com.healthpocket.data.remote.dto.AuthResponse
import com.healthpocket.data.remote.dto.UserResponse
import com.healthpocket.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with empty email shows error`() = runTest {
        viewModel.login("", "password123")
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Please fill in all fields", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with empty password shows error`() = runTest {
        viewModel.login("test@example.com", "")
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Please fill in all fields", state.error)
    }

    @Test
    fun `register with short password shows error`() = runTest {
        viewModel.register("test@example.com", "short", "John", "Doe")
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Password must be at least 8 characters", state.error)
    }

    @Test
    fun `register with empty fields shows error`() = runTest {
        viewModel.register("", "password123", "John", "Doe")
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Please fill in all fields", state.error)
    }

    @Test
    fun `clearError clears the error state`() = runTest {
        viewModel.login("", "")
        assertNotNull(viewModel.uiState.value.error)
        
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `resetState resets to initial state`() = runTest {
        viewModel.login("", "")
        
        viewModel.resetState()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSuccess)
    }
}

