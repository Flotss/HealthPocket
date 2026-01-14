package com.healthpocket.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthpocket.BuildConfig
import com.healthpocket.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class AddAppointmentUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val locationSuggestions: List<String> = emptyList(),
    val isLocationLookupInProgress: Boolean = false,
    val locationLookupError: String? = null,
    val showNoLocationResults: Boolean = false
)

@HiltViewModel
class AddAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAppointmentUiState())
    val uiState: StateFlow<AddAppointmentUiState> = _uiState.asStateFlow()
    private var locationLookupJob: Job? = null

    fun saveAppointment(
        title: String,
        doctorName: String?,
        location: String?,
        description: String?,
        appointmentDate: Long,
        durationMinutes: Int = 30,
        reminderMinutesBefore: Int = 60,
        reminderEnabled: Boolean = true,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                appointmentRepository.createAppointment(
                    title = title,
                    description = description,
                    doctorName = doctorName,
                    location = location,
                    appointmentDate = appointmentDate,
                    durationMinutes = durationMinutes,
                    reminderMinutesBefore = reminderMinutesBefore,
                    reminderEnabled = reminderEnabled,
                    notes = notes
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun searchLocationSuggestions(query: String) {
        locationLookupJob?.cancel()

        if (query.length < MIN_QUERY_LENGTH) {
            clearLocationSuggestions()
            return
        }

        locationLookupJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update {
                it.copy(
                    isLocationLookupInProgress = true,
                    locationLookupError = null,
                    showNoLocationResults = false
                )
            }

            val suggestions = try {
                fetchLocationSuggestions(query)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        locationSuggestions = emptyList(),
                        isLocationLookupInProgress = false,
                        locationLookupError = e.message ?: "Unable to fetch locations",
                        showNoLocationResults = false
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    locationSuggestions = suggestions,
                    isLocationLookupInProgress = false,
                    locationLookupError = null,
                    showNoLocationResults = suggestions.isEmpty()
                )
            }
        }
    }

    fun clearLocationSuggestions() {
        locationLookupJob?.cancel()
        _uiState.update {
            it.copy(
                locationSuggestions = emptyList(),
                isLocationLookupInProgress = false,
                locationLookupError = null,
                showNoLocationResults = false
            )
        }
    }

    private suspend fun fetchLocationSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val url = URL(
            "$GEOAPIFY_AUTOCOMPLETE_URL?text=$encodedQuery&limit=$AUTOCOMPLETE_LIMIT&apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            connectTimeout = REQUEST_TIMEOUT_MS
            readTimeout = REQUEST_TIMEOUT_MS
        }

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext emptyList()
            }

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            return@withContext parseLocationSuggestions(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseLocationSuggestions(payload: String): List<String> {
        val root = JSONObject(payload)
        val suggestions = mutableListOf<String>()

        root.optJSONArray("results")?.let { resultsArray ->
            for (index in 0 until resultsArray.length()) {
                val result = resultsArray.optJSONObject(index) ?: continue
                val formatted = result.optString("formatted")
                if (formatted.isNotBlank()) {
                    suggestions.add(formatted)
                }
            }
        }

        root.optJSONArray("features")?.let { featuresArray ->
            for (index in 0 until featuresArray.length()) {
                val feature = featuresArray.optJSONObject(index) ?: continue
                val properties = feature.optJSONObject("properties")
                val formatted = properties?.optString("formatted")
                    ?: feature.optString("formatted")
                if (formatted.isNotBlank()) {
                    suggestions.add(formatted)
                }
            }
        }

        return suggestions.distinct()
    }

    companion object {
        private const val GEOAPIFY_AUTOCOMPLETE_URL = "https://api.geoapify.com/v1/geocode/autocomplete"
        private const val AUTOCOMPLETE_LIMIT = 5
        private const val MIN_QUERY_LENGTH = 3
        private const val REQUEST_TIMEOUT_MS = 5000
        private const val SEARCH_DEBOUNCE_MS = 350L
    }
}
