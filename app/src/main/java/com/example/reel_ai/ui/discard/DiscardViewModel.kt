package com.example.reel_ai.ui.discard

import android.util.Log
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

private const val TAG = "DiscardViewModel"

@HiltViewModel
class DiscardViewModel @Inject constructor(
    private val videoManager: VideoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscardUiState>(DiscardUiState.Loading)
    val uiState: StateFlow<DiscardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DiscardEvent>()
    val events: SharedFlow<DiscardEvent> = _events.asSharedFlow()

    fun initializeWithFile(file: File) {
        viewModelScope.launch {
            if (!videoManager.validateVideo(file)) {
                _uiState.value = DiscardUiState.Error("Invalid video file")
                _events.emit(DiscardEvent.ShowError("Video file is invalid or inaccessible"))
                return@launch
            }
            _uiState.value = DiscardUiState.Ready(file)
        }
    }

    fun confirmDiscard() {
        val currentState = _uiState.value
        if (currentState !is DiscardUiState.Ready || currentState.isDeleting) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isDeleting = true)
            
            if (videoManager.deleteVideo(currentState.file)) {
                _events.emit(DiscardEvent.NavigateToFeed)
            } else {
                _uiState.value = currentState.copy(isDeleting = false)
                _events.emit(DiscardEvent.ShowError("Failed to delete video"))
            }
        }
    }

    fun cancelDiscard() {
        viewModelScope.launch {
            _events.emit(DiscardEvent.NavigateBack)
        }
    }
} 