package com.healthpocket.ui.medications

import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.repository.MedicationRepository
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationsViewModelTest {

    private lateinit var viewModel: MedicationsViewModel
    private lateinit var medicationRepository: MedicationRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = mock()
        whenever(medicationRepository.getAllMedications()).thenReturn(flowOf(emptyList()))
        viewModel = MedicationsViewModel(medicationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteMedication calls repository deleteMedication`() = runTest {
        val medication = createMedication()
        whenever(medicationRepository.deleteMedication(any())).thenReturn(Unit)

        viewModel.deleteMedication(medication)

        verify(medicationRepository).deleteMedication(medication)
    }

    private fun createMedication(): MedicationEntity {
        return MedicationEntity(
            id = "test-id",
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "08:00",
            startDate = LocalDate.now(),
            isActive = true,
            syncStatus = SyncStatus.SYNCED
        )
    }
}
