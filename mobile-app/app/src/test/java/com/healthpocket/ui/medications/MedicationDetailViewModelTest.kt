package com.healthpocket.ui.medications

import androidx.lifecycle.SavedStateHandle
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.repository.MedicationRepository
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDetailViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: MedicationDetailViewModel
    private lateinit var medicationRepository: MedicationRepository

    @Before
    fun setUp() {
        medicationRepository = mock()
        val savedStateHandle = SavedStateHandle(mapOf("medicationId" to "test-id"))
        whenever(medicationRepository.getMedicationById(any())).thenReturn(flowOf(createMedication()))
        viewModel = MedicationDetailViewModel(medicationRepository, savedStateHandle)
    }

    @Test
    fun `toggleMedicationStatus updates medication`() = runTest {
        val medication = createMedication()
        whenever(medicationRepository.getMedicationById(any())).thenReturn(flowOf(medication))
        whenever(medicationRepository.updateMedication(any())).thenReturn(Unit)

        viewModel.toggleMedicationStatus()

        verify(medicationRepository).updateMedication(any())
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
