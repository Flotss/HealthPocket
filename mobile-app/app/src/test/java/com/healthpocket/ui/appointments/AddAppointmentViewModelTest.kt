package com.healthpocket.ui.appointments

import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.repository.AppointmentRepository
import com.healthpocket.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AddAppointmentViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: AddAppointmentViewModel
    private lateinit var appointmentRepository: AppointmentRepository

    @Before
    fun setUp() {
        appointmentRepository = mock()
        viewModel = AddAppointmentViewModel(appointmentRepository)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
        assertNull(state.error)
        assertEquals(emptyList<String>(), state.locationSuggestions)
        assertFalse(state.isLocationLookupInProgress)
        assertNull(state.locationLookupError)
        assertFalse(state.showNoLocationResults)
    }

    @Test
    fun `saveAppointment sets loading state`() = runTest {
        val appointment = createAppointment()
        whenever(appointmentRepository.createAppointment(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(appointment)

        viewModel.saveAppointment(
            title = "Test Appointment",
            doctorName = "Dr. Smith",
            location = "Hospital",
            description = "Description",
            appointmentDate = System.currentTimeMillis(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            notes = "Notes"
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSaved)
    }


    @Test
    fun `clearLocationSuggestions clears suggestions`() = runTest {
        viewModel.clearLocationSuggestions()
        
        val state = viewModel.uiState.value
        assertEquals(emptyList<String>(), state.locationSuggestions)
        assertFalse(state.isLocationLookupInProgress)
        assertNull(state.locationLookupError)
        assertFalse(state.showNoLocationResults)
    }

    private fun createAppointment(): AppointmentEntity {
        return AppointmentEntity(
            id = "test-id",
            title = "Test Appointment",
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = System.currentTimeMillis(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = AppointmentStatus.SCHEDULED,
            notes = "Notes",
            syncStatus = com.healthpocket.data.local.entity.SyncStatus.SYNCED
        )
    }
}
