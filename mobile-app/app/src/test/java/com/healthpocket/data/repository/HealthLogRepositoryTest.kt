package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.HealthLogDao
import com.healthpocket.data.local.entity.HealthLogEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.HealthLogResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HealthLogRepositoryTest {

    private lateinit var repository: HealthLogRepository
    private lateinit var healthLogDao: HealthLogDao
    private lateinit var api: HealthPocketApi

    @Before
    fun setUp() {
        healthLogDao = mock()
        api = mock()
        repository = HealthLogRepository(healthLogDao, api)
    }

    @Test
    fun `getAllHealthLogs returns flow from dao`() = runTest {
        val logs = listOf(
            createHealthLog("log1"),
            createHealthLog("log2")
        )
        whenever(healthLogDao.getAllHealthLogs()).thenReturn(flowOf(logs))

        val result = repository.getAllHealthLogs()

        assertNotNull(result)
    }

    @Test
    fun `getRecentHealthLogs returns flow from dao`() = runTest {
        val logs = listOf(createHealthLog("log1"))
        whenever(healthLogDao.getRecentHealthLogs(any())).thenReturn(flowOf(logs))

        val result = repository.getRecentHealthLogs(7)

        assertNotNull(result)
    }

    @Test
    fun `getHealthLogByDate returns flow from dao`() = runTest {
        val log = createHealthLog("log1")
        whenever(healthLogDao.getHealthLogByDate(any())).thenReturn(flowOf(log))

        val result = repository.getHealthLogByDate(LocalDate.now())

        assertNotNull(result)
    }

    @Test
    fun `createOrUpdateHealthLog creates new log when none exists`() = runTest {
        whenever(healthLogDao.getHealthLogByDateSync(any())).thenReturn(null)
        whenever(healthLogDao.insert(any())).thenReturn(Unit)
        whenever(api.createOrUpdateHealthLog(any())).thenReturn(
            Response.success(createHealthLogResponse())
        )

        val result = repository.createOrUpdateHealthLog(
            date = LocalDate.now(),
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = 7.5f,
            symptoms = listOf("Headache"),
            notes = "Feeling good"
        )

        assertNotNull(result)
        assertEquals(5, result.mood)
        assertEquals(SyncStatus.PENDING, result.syncStatus)
        verify(healthLogDao).insert(any())
    }

    @Test
    fun `createOrUpdateHealthLog updates existing log`() = runTest {
        val existingLog = createHealthLog("log1")
        whenever(healthLogDao.getHealthLogByDateSync(any())).thenReturn(existingLog)
        whenever(healthLogDao.insert(any())).thenReturn(Unit)
        whenever(api.createOrUpdateHealthLog(any())).thenReturn(
            Response.success(createHealthLogResponse())
        )

        val result = repository.createOrUpdateHealthLog(
            date = LocalDate.now(),
            mood = 5
        )

        assertNotNull(result)
        assertEquals(5, result.mood)
        verify(healthLogDao).insert(any())
    }

    @Test
    fun `deleteHealthLog removes from dao`() = runTest {
        val log = createHealthLog("log1", serverId = UUID.randomUUID())
        whenever(healthLogDao.delete(any())).thenReturn(Unit)
        whenever(api.deleteHealthLog(any())).thenReturn(Response.success(Unit))

        repository.deleteHealthLog(log)

        verify(healthLogDao).delete(log)
    }

    @Test
    fun `getPendingHealthLogs returns pending logs`() = runTest {
        val pendingLogs = listOf(createHealthLog("pending"))
        whenever(healthLogDao.getHealthLogsBySyncStatus(SyncStatus.PENDING))
            .thenReturn(pendingLogs)

        val result = repository.getPendingHealthLogs()

        assertEquals(1, result.size)
        assertEquals("pending", result[0].id)
    }

    private fun createHealthLog(
        id: String,
        serverId: UUID? = null
    ): HealthLogEntity {
        return HealthLogEntity(
            id = id,
            serverId = serverId?.toString(),
            logDate = LocalDate.now(),
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = 7.5f,
            symptoms = "Headache",
            notes = "Test log",
            syncStatus = SyncStatus.SYNCED
        )
    }

    private fun createHealthLogResponse(): HealthLogResponse {
        val now = OffsetDateTime.now()
        return HealthLogResponse(
            id = UUID.randomUUID().toString(),
            logDate = LocalDate.now().toString(),
            mood = 5,
            energyLevel = 4,
            sleepQuality = 3,
            sleepHours = 7.5f,
            symptoms = listOf("Headache"),
            notes = "Test log",
            syncStatus = "SYNCED",
            localId = null,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
    }
}
