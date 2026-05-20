// File: app/src/main/java/com/example/weathersnap/presentation/report/CreateReportViewModel.kt
package com.example.weathersnap.presentation.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.domain.model.WeatherSnapshot
import com.example.weathersnap.domain.model.WeatherReport
import com.example.weathersnap.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateReportUiState(
    val capturedImagePath: String? = null,
    val originalSizeKb: Long = 0L,
    val compressedSizeKb: Long = 0L,
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed class CreateReportEvent {
    object SaveSuccess : CreateReportEvent()
    data class ShowError(val message: String) : CreateReportEvent()
}

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateReportEvent>()
    val events = _events.asSharedFlow()

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onImageCaptured(path: String, originalSizeKb: Long, compressedSizeKb: Long) {
        _uiState.update {
            it.copy(
                capturedImagePath = path,
                originalSizeKb = originalSizeKb,
                compressedSizeKb = compressedSizeKb
            )
        }
    }

    fun saveReport(snapshot: WeatherSnapshot) {
        val currentState = _uiState.value

        if (currentState.capturedImagePath == null) {
            viewModelScope.launch {
                _events.emit(CreateReportEvent.ShowError("Please capture a photo first!"))
            }
            return
        }

        if (currentState.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val report = WeatherReport(
                    city = snapshot.cityName,       // ✅ cityName
                    temperature = snapshot.temperature,
                    condition = snapshot.condition,
                    humidity = snapshot.humidity,
                    windSpeed = snapshot.windSpeed, // ✅ windSpeed
                    pressure = snapshot.pressure,
                    notes = currentState.notes,
                    imagePath = currentState.capturedImagePath,
                    originalSizeKb = currentState.originalSizeKb,
                    compressedSizeKb = currentState.compressedSizeKb,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveReport(report)
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(CreateReportEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage) }
                _events.emit(CreateReportEvent.ShowError("Failed to save. Try again."))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}