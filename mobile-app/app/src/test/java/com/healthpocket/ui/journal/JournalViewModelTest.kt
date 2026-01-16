package com.healthpocket.ui.journal

import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.repository.HealthLogRepository
import com.healthpocket.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: JournalViewModel
    private lateinit var healthLogRepository: HealthLogRepository

    @Before
    fun setUp() {
        healthLogRepository = mock()
        whenever(healthLogRepository.getRecentHealthLogs(any())).thenReturn(flowOf(emptyList()))
        viewModel = JournalViewModel(healthLogRepository)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        
        assertNull(state.selectedMood)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `setMood updates selected mood`() = runTest {
        viewModel.setMood(5)
        
        val state = viewModel.uiState.value
        assertEquals(5, state.selectedMood)
    }

    @Test
    fun `saveTodayLog saves log with mood`() = runTest {
        viewModel.setMood(5)
        val healthLog = createHealthLog()
        whenever(healthLogRepository.createOrUpdateHealthLog(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(healthLog)

        viewModel.saveTodayLog()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.selectedMood)
    }

    @Test
    fun `saveTodayLog does nothing when mood is not set`() = runTest {
        viewModel.saveTodayLog()

        val state = viewModel.uiState.value
        assertNull(state.selectedMood)
    }

    @Test
    fun `saveDetailedLog saves log with all fields`() = runTest {
        val healthLog = createHealthLog()
        whenever(healthLogRepository.createOrUpdateHealthLog(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(healthLog)

        viewModel.saveDetailedLog(
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = 7.5f,
            notes = "Feeling good"
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }


    private fun createHealthLog(): HealthLogEntity {
        return HealthLogEntity(
            id = "test-id",
            logDate = LocalDate.now(),
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = 7.5f,
            symptoms = null,
            notes = "Test log",
            syncStatus = SyncStatus.SYNCED
        )
    }
}
