package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.VitalMetricDao
import com.healthpocket.data.local.entity.MetricType
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.local.entity.VitalMetricEntity
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.VitalMetricResponse
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
class VitalMetricRepositoryTest {

    private lateinit var repository: VitalMetricRepository
    private lateinit var vitalMetricDao: VitalMetricDao
    private lateinit var api: HealthPocketApi

    @Before
    fun setUp() {
        vitalMetricDao = mock()
        api = mock()
        repository = VitalMetricRepository(vitalMetricDao, api)
    }

    @Test
    fun `getAllVitalMetrics returns flow from dao`() = runTest {
        val metrics = listOf(
            createMetric("metric1"),
            createMetric("metric2")
        )
        whenever(vitalMetricDao.getAllVitalMetrics()).thenReturn(flowOf(metrics))

        val result = repository.getAllVitalMetrics()

        assertNotNull(result)
    }

    @Test
    fun `getVitalMetricsByType returns flow from dao`() = runTest {
        val metrics = listOf(createMetric("metric1"))
        whenever(vitalMetricDao.getVitalMetricsByType(any())).thenReturn(flowOf(metrics))

        val result = repository.getVitalMetricsByType(MetricType.WEIGHT)

        assertNotNull(result)
    }

    @Test
    fun `getRecentVitalMetricsByType returns flow from dao`() = runTest {
        val metrics = listOf(createMetric("metric1"))
        whenever(vitalMetricDao.getRecentVitalMetricsByType(any(), any())).thenReturn(flowOf(metrics))

        val result = repository.getRecentVitalMetricsByType(MetricType.WEIGHT, 10)

        assertNotNull(result)
    }

    @Test
    fun `getLatestVitalMetricByType returns flow from dao`() = runTest {
        val metric = createMetric("metric1")
        whenever(vitalMetricDao.getLatestVitalMetricByType(any())).thenReturn(flowOf(metric))

        val result = repository.getLatestVitalMetricByType(MetricType.WEIGHT)

        assertNotNull(result)
    }

    @Test
    fun `getVitalMetricsInRange returns flow from dao`() = runTest {
        val metrics = listOf(createMetric("metric1"))
        whenever(vitalMetricDao.getVitalMetricsInRange(any(), any())).thenReturn(flowOf(metrics))

        val result = repository.getVitalMetricsInRange(0L, 1000L)

        assertNotNull(result)
    }

    @Test
    fun `createVitalMetric saves to dao with pending sync status`() = runTest {
        whenever(vitalMetricDao.insert(any())).thenReturn(Unit)
        whenever(api.createVitalMetric(any())).thenReturn(
            Response.success(createMetricResponse())
        )

        val result = repository.createVitalMetric(
            metricType = MetricType.WEIGHT,
            value = 70.5f,
            secondaryValue = null,
            unit = "kg",
            measuredAt = System.currentTimeMillis(),
            notes = "Morning weight"
        )

        assertNotNull(result)
        assertEquals(MetricType.WEIGHT, result.metricType)
        assertEquals(70.5f, result.value)
        assertEquals(SyncStatus.PENDING, result.syncStatus)
        verify(vitalMetricDao).insert(any())
    }

    @Test
    fun `deleteVitalMetric removes from dao`() = runTest {
        val metric = createMetric("metric1", serverId = UUID.randomUUID())
        whenever(vitalMetricDao.delete(any())).thenReturn(Unit)
        whenever(api.deleteVitalMetric(any())).thenReturn(Response.success(Unit))

        repository.deleteVitalMetric(metric)

        verify(vitalMetricDao).delete(metric)
    }

    @Test
    fun `getPendingVitalMetrics returns pending metrics`() = runTest {
        val pendingMetrics = listOf(createMetric("pending"))
        whenever(vitalMetricDao.getVitalMetricsBySyncStatus(SyncStatus.PENDING))
            .thenReturn(pendingMetrics)

        val result = repository.getPendingVitalMetrics()

        assertEquals(1, result.size)
        assertEquals("pending", result[0].id)
    }

    private fun createMetric(
        id: String,
        serverId: UUID? = null
    ): VitalMetricEntity {
        return VitalMetricEntity(
            id = id,
            serverId = serverId?.toString(),
            metricType = MetricType.WEIGHT,
            value = 70.5f,
            secondaryValue = null,
            unit = "kg",
            measuredAt = System.currentTimeMillis(),
            notes = "Test metric",
            syncStatus = SyncStatus.SYNCED
        )
    }

    private fun createMetricResponse(): VitalMetricResponse {
        val now = OffsetDateTime.now()
        return VitalMetricResponse(
            id = UUID.randomUUID().toString(),
            metricType = "WEIGHT",
            value = 70.5f,
            secondaryValue = null,
            unit = "kg",
            measuredAt = now.toString(),
            notes = "Test metric",
            syncStatus = "SYNCED",
            localId = null,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
    }
}
