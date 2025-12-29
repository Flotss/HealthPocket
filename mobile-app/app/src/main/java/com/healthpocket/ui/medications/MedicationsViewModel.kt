package com.healthpocket.ui.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.data.local.entity.MedicationEntity
import com.healthpocket.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the medications list screen.
 */
@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val medications: Flow<List<MedicationEntity>> = medicationRepository.getAllMedications()

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)
        }
    }
}

