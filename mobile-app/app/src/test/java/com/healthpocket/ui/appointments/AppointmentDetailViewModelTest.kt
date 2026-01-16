package com.healthpocket.ui.appointments

import androidx.lifecycle.SavedStateHandle
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.repository.AppointmentRepository
import com.healthpocket.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentDetailViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: AppointmentDetailViewModel
    private lateinit var appointmentRepository: AppointmentRepository

    @Before
    fun setUp() {
        appointmentRepository = mock()
        val savedStateHandle = SavedStateHandle(mapOf("appointmentId" to "test-id"))
        whenever(appointmentRepository.getAppointmentById(any())).thenReturn(flowOf(createAppointment()))
        viewModel = AppointmentDetailViewModel(appointmentRepository, savedStateHandle)
    }

    @Test
    fun `markAsCompleted calls repository updateAppointmentStatus`() = runTest {
        whenever(appointmentRepository.updateAppointmentStatus(any(), any())).thenReturn(Unit)

        viewModel.markAsCompleted()

        verify(appointmentRepository).updateAppointmentStatus("test-id", AppointmentStatus.COMPLETED)
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
