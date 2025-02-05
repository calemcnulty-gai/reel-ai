package com.example.reel_ai.ui.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.video.VideoManager
import com.example.reel_ai.domain.repository.VideoRepository
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

private const val TAG = "EditViewModel"

@HiltViewModel
class EditViewModel @Inject constructor(
    private val videoManager: VideoManager,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditEvent>()
    val events: SharedFlow<EditEvent> = _events.asSharedFlow()

    fun initializeWithFile(file: File) {
        viewModelScope.launch {
            Log.d(TAG, "Initializing with file: ${file.absolutePath}")
            if (!videoManager.validateVideo(file)) {
                Log.e(TAG, "Invalid video file: ${file.absolutePath}")
                _uiState.value = EditUiState.Error(
                    message = "Invalid video file",
                    file = file
                )
                _events.emit(EditEvent.ShowError("Video file is invalid or inaccessible"))
                return@launch
            }
            Log.d(TAG, "Video file validated successfully")
            _uiState.value = EditUiState.Ready(file)
        }
    }

    fun updateTitle(title: String) {
        val currentState = _uiState.value
        if (currentState is EditUiState.Ready && !currentState.isProcessing) {
            Log.d(TAG, "Updating title to: $title")
            _uiState.value = currentState.copy(title = title)
        }
    }

    fun updateDescription(description: String) {
        val currentState = _uiState.value
        if (currentState is EditUiState.Ready && !currentState.isProcessing) {
            Log.d(TAG, "Updating description to: $description")
            _uiState.value = currentState.copy(description = description)
        }
    }

    fun processVideo() {
        val currentState = _uiState.value
        if (currentState !is EditUiState.Ready || currentState.isProcessing) {
            Log.w(TAG, "Cannot process video: Invalid state or already processing")
            return
        }

        if (currentState.title.isBlank()) {
            viewModelScope.launch {
                Log.w(TAG, "Cannot process video: Title is required")
                _events.emit(EditEvent.ShowError("Please enter a title for your video"))
            }
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Returning to upload with metadata")
            Log.d(TAG, "Title: ${currentState.title}")
            Log.d(TAG, "Description: ${currentState.description}")
            
            _events.emit(EditEvent.NavigateToUpload(
                file = currentState.file,
                title = currentState.title,
                description = currentState.description
            ))
        }
    }

    fun cancelEdit() {
        viewModelScope.launch {
            Log.d(TAG, "Cancelling edit")
            _events.emit(EditEvent.NavigateBack)
        }
    }

    fun retryEdit() {
        val currentState = _uiState.value
        if (currentState is EditUiState.Error) {
            Log.d(TAG, "Retrying edit")
            _uiState.value = EditUiState.Ready(currentState.file)
        }
    }
} 