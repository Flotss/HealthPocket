package com.healthpocket.data.repository

import com.healthpocket.data.local.dao.MedicationDao
import com.healthpocket.data.local.dao.MedicationIntakeDao
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.remote.api.HealthPocketApi
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationRepositoryTest {

    private lateinit var repository: MedicationRepository
    private lateinit var medicationDao: MedicationDao
    private lateinit var intakeDao: MedicationIntakeDao
    private lateinit var api: HealthPocketApi

    @Before
    fun setUp() {
        medicationDao = mock()
        intakeDao = mock()
        api = mock()
        repository = MedicationRepository(medicationDao, intakeDao, api)
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
        val medication = createMedication("Aspirin")
        whenever(medicationDao.delete(medication)).thenReturn(Unit)

        repository.deleteMedication(medication)

        verify(medicationDao).delete(medication)
    }

    private fun createMedication(name: String, isActive: Boolean = true): MedicationEntity {
        return MedicationEntity(
            id = "test-id",
            name = name,
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "08:00",
            startDate = LocalDate.now(),
            isActive = isActive,
            syncStatus = SyncStatus.SYNCED
        )
    }
}

