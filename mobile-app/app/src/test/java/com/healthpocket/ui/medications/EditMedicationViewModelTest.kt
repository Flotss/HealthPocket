package com.healthpocket.ui.medications

import androidx.lifecycle.SavedStateHandle
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
class EditMedicationViewModelTest {

    private lateinit var viewModel: EditMedicationViewModel
    private lateinit var medicationRepository: MedicationRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = mock()
        val savedStateHandle = SavedStateHandle(mapOf("medicationId" to "test-id"))
        whenever(medicationRepository.getMedicationById(any())).thenReturn(flowOf(createMedication()))
        viewModel = EditMedicationViewModel(medicationRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveMedication updates medication`() = runTest {
        val medication = createMedication()
        whenever(medicationRepository.getMedicationById(any())).thenReturn(flowOf(medication))
        whenever(medicationRepository.updateMedication(any())).thenReturn(Unit)

        viewModel.saveMedication(
            name = "Updated Aspirin",
            dosage = "1000mg",
            frequency = "Twice daily",
            scheduleTimes = listOf("08:00", "20:00"),
            endDate = null,
            notes = "Updated notes",
            color = "#FF0000",
            reminderEnabled = true
        )

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
