package com.healthpocket.ui.medications

import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.local.entity.SyncStatus
import com.healthpocket.data.repository.MedicationRepository
import com.healthpocket.ui.theme.AppColors
import com.healthpocket.util.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddMedicationViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: AddMedicationViewModel
    private lateinit var medicationRepository: MedicationRepository

    @Before
    fun setUp() {
        medicationRepository = mock()
        viewModel = AddMedicationViewModel(medicationRepository)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
        assertNull(state.error)
    }

    @Test
    fun `saveMedication sets loading and saved state`() = runTest {
        val medication = createMedication()
        whenever(medicationRepository.createMedication(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(medication)

        viewModel.saveMedication(
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = listOf("08:00", "20:00"),
            startDate = LocalDate.now(),
            endDate = null,
            notes = "Take with food",
            color = AppColors.MEDICATION_BLUE,
            reminderEnabled = true
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSaved)
    }


    private fun createMedication(): MedicationEntity {
        return MedicationEntity(
            id = "test-id",
            name = "Aspirin",
            dosage = "500mg",
            frequency = "Daily",
            scheduleTimes = "08:00,20:00",
            startDate = LocalDate.now(),
            isActive = true,
            syncStatus = SyncStatus.SYNCED
        )
    }
}
