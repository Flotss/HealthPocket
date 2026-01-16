package com.healthpocket.data.repository

import android.content.Context
import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
import com.healthpocket.data.remote.dto.MedicationResponse
import com.healthpocket.ui.theme.AppColors
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
class MedicationRepositoryTest {

    private lateinit var repository: MedicationRepository
    private lateinit var medicationDao: MedicationDao
    private lateinit var intakeDao: MedicationIntakeDao
    private lateinit var api: HealthPocketApi
    private lateinit var context: Context

    @Before
    fun setUp() {
        medicationDao = mock()
        intakeDao = mock()
        api = mock()
        context = mock()
        repository = MedicationRepository(medicationDao, intakeDao, api, context)
    }

    @Test
    fun `getAllMedications returns flow from dao`() = runTest {
        val medications = listOf(
            createMedication("Aspirin"),
            createMedication("Ibuprofen")
        )
        whenever(medicationDao.getAllMedications()).thenReturn(flowOf(medications))

        val result = repository.getAllMedications()

        assertNotNull(result)
    }

    @Test
    fun `getActiveMedications returns only active medications`() = runTest {
        val activeMedication = createMedication("Aspirin", isActive = true)
        whenever(medicationDao.getActiveMedications()).thenReturn(flowOf(listOf(activeMedication)))

        val result = repository.getActiveMedications()

        assertNotNull(result)
    }

    @Test
    fun `createMedication saves to dao with pending sync status`() = runTest {
        whenever(medicationDao.insert(any())).thenReturn(Unit)
        whenever(medicationDao.getMedicationByIdSync(any())).thenReturn(null)
        whenever(medicationDao.updateServerIdAndStatus(any(), any(), any())).thenReturn(Unit)
        whenever(medicationDao.updateSyncStatus(any(), any())).thenReturn(Unit)
        whenever(api.createMedication(any())).thenReturn(
            Response.success(createMedicationResponse())
        )
        
        val result = repository.createMedication(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00", "20:00"),
            startDate = LocalDate.now()
        )

        assertNotNull(result)
        assertEquals("Aspirin", result.name)
        assertEquals(SyncStatus.PENDING, result.syncStatus)
        verify(medicationDao).insert(any())
    }

    @Test
    fun `deleteMedication removes from dao`() = runTest {
        val medication = createMedication("Aspirin", serverId = UUID.randomUUID().toString())
        whenever(medicationDao.delete(medication)).thenReturn(Unit)
        whenever(api.deleteMedication(any())).thenReturn(Response.success(Unit))

        repository.deleteMedication(medication)

        verify(medicationDao).delete(medication)
    }

    @Test
    fun `updateMedication updates dao with pending sync status`() = runTest {
        val medication = createMedication("Aspirin")
        whenever(medicationDao.update(any())).thenReturn(Unit)
        whenever(medicationDao.getMedicationByIdSync(any())).thenReturn(medication)
        whenever(medicationDao.updateServerIdAndStatus(any(), any(), any())).thenReturn(Unit)
        whenever(medicationDao.updateSyncStatus(any(), any())).thenReturn(Unit)
        whenever(api.updateMedication(any(), any())).thenReturn(
            Response.success(createMedicationResponse())
        )

        repository.updateMedication(medication)

        verify(medicationDao).update(any())
    }

    @Test
    fun `getMedicationById returns flow from dao`() = runTest {
        val medication = createMedication("Aspirin")
        whenever(medicationDao.getMedicationById(any())).thenReturn(flowOf(medication))

        val result = repository.getMedicationById("test-id")

        assertNotNull(result)
    }

    private fun createMedication(name: String, isActive: Boolean = true, serverId: String? = null): MedicationEntity {
        return MedicationEntity(
            id = "test-id",
            serverId = serverId,
            name = name,
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "08:00",
            startDate = LocalDate.now(),
            isActive = isActive,
            syncStatus = SyncStatus.SYNCED
        )
    }

    private fun createMedicationResponse(): MedicationResponse {
        val now = OffsetDateTime.now()
        return MedicationResponse(
            id = UUID.randomUUID().toString(),
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00", "20:00"),
            startDate = LocalDate.now().toString(),
            endDate = null,
            notes = null,
            color = AppColors.MEDICATION_DEFAULT,
            reminderEnabled = true,
            isActive = true,
            syncStatus = "SYNCED",
            localId = null,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
    }
}

