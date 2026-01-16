package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.AppointmentDao
import com.healthpocket.data.local.entity.AppointmentEntity
import com.healthpocket.data.local.entity.AppointmentStatus
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.AppointmentResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.time.OffsetDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentRepositoryTest {

    private lateinit var repository: AppointmentRepository
    private lateinit var appointmentDao: AppointmentDao
    private lateinit var api: HealthPocketApi

    @Before
    fun setUp() {
        appointmentDao = mock()
        api = mock()
        repository = AppointmentRepository(appointmentDao, api)
    }

    @Test
    fun `getAllAppointments returns flow from dao`() = runTest {
        val appointments = listOf(
            createAppointment("Appointment 1"),
            createAppointment("Appointment 2")
        )
        whenever(appointmentDao.getAllAppointments()).thenReturn(flowOf(appointments))

        val result = repository.getAllAppointments()

        assertNotNull(result)
    }

    @Test
    fun `getUpcomingAppointments returns flow from dao`() = runTest {
        val appointments = listOf(createAppointment("Upcoming"))
        whenever(appointmentDao.getUpcomingAppointments(any())).thenReturn(flowOf(appointments))

        val result = repository.getUpcomingAppointments()

        assertNotNull(result)
    }

    @Test
    fun `getPastAppointments returns flow from dao`() = runTest {
        val appointments = listOf(createAppointment("Past"))
        whenever(appointmentDao.getPastAppointments(any())).thenReturn(flowOf(appointments))

        val result = repository.getPastAppointments()

        assertNotNull(result)
    }

    @Test
    fun `getAppointmentsInRange returns flow from dao`() = runTest {
        val appointments = listOf(createAppointment("Range"))
        whenever(appointmentDao.getAppointmentsInRange(any(), any())).thenReturn(flowOf(appointments))

        val result = repository.getAppointmentsInRange(0L, 1000L)

        assertNotNull(result)
    }

    @Test
    fun `getAppointmentById returns flow from dao`() = runTest {
        val appointment = createAppointment("Test")
        whenever(appointmentDao.getAppointmentById(any())).thenReturn(flowOf(appointment))

        val result = repository.getAppointmentById("test-id")

        assertNotNull(result)
    }

    @Test
    fun `createAppointment saves to dao with pending sync status`() = runTest {
        whenever(appointmentDao.insert(any())).thenReturn(Unit)
        whenever(api.createAppointment(any())).thenReturn(
            Response.success(createAppointmentResponse())
        )

        val result = repository.createAppointment(
            title = "Test Appointment",
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = System.currentTimeMillis(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            notes = "Notes"
        )

        assertNotNull(result)
        assertEquals("Test Appointment", result.title)
        assertEquals(SyncStatus.PENDING, result.syncStatus)
        verify(appointmentDao).insert(any())
    }

    @Test
    fun `updateAppointment updates dao with pending sync status`() = runTest {
        val appointment = createAppointment("Test")
        whenever(appointmentDao.update(any())).thenReturn(Unit)
        whenever(api.updateAppointment(any(), any())).thenReturn(
            Response.success(createAppointmentResponse())
        )

        repository.updateAppointment(appointment)

        verify(appointmentDao).update(any())
    }

    @Test
    fun `updateAppointmentStatus updates status in dao`() = runTest {
        val appointment = createAppointment("Test")
        whenever(appointmentDao.updateStatus(any(), any(), any())).thenReturn(Unit)
        whenever(appointmentDao.getAppointmentByIdSync(any())).thenReturn(appointment)
        whenever(api.updateAppointment(any(), any())).thenReturn(
            Response.success(createAppointmentResponse())
        )

        repository.updateAppointmentStatus("test-id", AppointmentStatus.COMPLETED)

        verify(appointmentDao).updateStatus("test-id", AppointmentStatus.COMPLETED, SyncStatus.PENDING)
    }

    @Test
    fun `deleteAppointment removes from dao`() = runTest {
        val appointment = createAppointment("Test", serverId = UUID.randomUUID())
        whenever(appointmentDao.delete(any())).thenReturn(Unit)
        whenever(api.deleteAppointment(any())).thenReturn(Response.success(Unit))

        repository.deleteAppointment(appointment)

        verify(appointmentDao).delete(appointment)
    }

    @Test
    fun `getPendingAppointments returns pending appointments`() = runTest {
        val pendingAppointments = listOf(createAppointment("Pending"))
        whenever(appointmentDao.getAppointmentsBySyncStatus(SyncStatus.PENDING))
            .thenReturn(pendingAppointments)

        val result = repository.getPendingAppointments()

        assertEquals(1, result.size)
        assertEquals("Pending", result[0].title)
    }

    private fun createAppointment(
        title: String,
        serverId: UUID? = null
    ): AppointmentEntity {
        return AppointmentEntity(
            id = UUID.randomUUID().toString(),
            serverId = serverId?.toString(),
            title = title,
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = System.currentTimeMillis(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = AppointmentStatus.SCHEDULED,
            notes = "Notes",
            syncStatus = SyncStatus.SYNCED
        )
    }

    private fun createAppointmentResponse(): AppointmentResponse {
        val now = OffsetDateTime.now()
        return AppointmentResponse(
            id = UUID.randomUUID().toString(),
            title = "Test Appointment",
            description = "Description",
            doctorName = "Dr. Smith",
            location = "Hospital",
            appointmentDate = now.toString(),
            durationMinutes = 30,
            reminderMinutesBefore = 60,
            reminderEnabled = true,
            status = "SCHEDULED",
            notes = "Notes",
            syncStatus = "SYNCED",
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
    }
}
