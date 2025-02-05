package com.example.reel_ai.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.video.VideoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val videoManager: VideoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PreviewEvent>()
    val events: SharedFlow<PreviewEvent> = _events.asSharedFlow()

    fun initializeWithFile(file: File) {
        viewModelScope.launch {
            if (!videoManager.validateVideo(file)) {
                _uiState.value = PreviewUiState.Error("Invalid video file")
                _events.emit(PreviewEvent.ShowError("Video file is invalid or inaccessible"))
                return@launch
            }
            _uiState.value = PreviewUiState.Success(file)
        }
    }

    fun onUploadClick(file: File) {
        viewModelScope.launch {
            _events.emit(PreviewEvent.NavigateToUpload(file))
        }
    }

    fun onEditClick(file: File) {
        viewModelScope.launch {
            _events.emit(PreviewEvent.NavigateToEdit(file))
        }
    }

    fun onDiscardClick(file: File) {
        viewModelScope.launch {
            _events.emit(PreviewEvent.NavigateToDiscard(file))
        }
    }

    fun togglePlayback() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            _uiState.value = currentState.copy(isPlaying = !currentState.isPlaying)
        }
    }
} 