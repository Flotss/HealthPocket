package com.healthpocket.ui.appointments

import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.repository.AppointmentRepository
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
class AppointmentsViewModelTest {

    private lateinit var viewModel: AppointmentsViewModel
    private lateinit var appointmentRepository: AppointmentRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appointmentRepository = mock()
        viewModel = AppointmentsViewModel(appointmentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `markAsCompleted calls repository updateAppointmentStatus`() = runTest {
        whenever(appointmentRepository.updateAppointmentStatus(any(), any())).thenReturn(Unit)

        viewModel.markAsCompleted("appointment-id")

        verify(appointmentRepository).updateAppointmentStatus("appointment-id", AppointmentStatus.COMPLETED)
    }

    @Test
    fun `deleteAppointment calls repository deleteAppointment`() = runTest {
        val appointment = createAppointment()
        whenever(appointmentRepository.deleteAppointment(any())).thenReturn(Unit)

        viewModel.deleteAppointment(appointment)

        verify(appointmentRepository).deleteAppointment(appointment)
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
