package com.example.pepperbell.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pepperbell.data.PredictionResponse
import com.example.pepperbell.tflite.TfLitePepperClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class PredictionUiState {
    object Idle : PredictionUiState()
    object Loading : PredictionUiState()
    data class Success(val result: PredictionResponse) : PredictionUiState()
    data class Error(val message: String) : PredictionUiState()
}

class PredictionViewModel(
    private val classifier: TfLitePepperClassifier
) : ViewModel() {
    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun predictImage(imageFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PredictionUiState.Loading
            try {
                val result: PredictionResponse = classifier.predict(imageFile)
                _uiState.value = PredictionUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = PredictionUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = PredictionUiState.Idle
    }
}
