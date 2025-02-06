package com.example.reel_ai.ui.upload

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.repository.VideoRepository
import com.example.reel_ai.domain.video.VideoManager
import com.example.reel_ai.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "ReelAI_UploadVM"

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val videoManager: VideoManager,
    private val videoRepository: VideoRepository
) : ViewModel() {

    init {
        Log.d(TAG, "=== UploadViewModel initialized ===")
    }

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Loading)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UploadEvent>()
    val events: SharedFlow<UploadEvent> = _events.asSharedFlow()

    private var uploadJob: Job? = null

    fun initializeWithFile(file: File, initialTitle: String? = null, initialDescription: String? = null) {
        Log.d(TAG, "Initializing with file: ${file.absolutePath}")
        Log.d(TAG, "Initial title: $initialTitle")
        Log.d(TAG, "Initial description: $initialDescription")
        
        if (!file.exists() || !file.canRead()) {
            Log.e(TAG, "File validation failed: exists=${file.exists()}, canRead=${file.canRead()}")
            _uiState.value = UploadUiState.Error("Video file is invalid or inaccessible")
            return
        }

        _uiState.value = UploadUiState.Ready(
            file = file,
            title = initialTitle ?: "",
            description = initialDescription ?: ""
        )
    }

    fun updateTitle(title: String) {
        val currentState = _uiState.value
        if (currentState is UploadUiState.Ready && !currentState.isUploading) {
            Log.d(TAG, "Updating title to: $title")
            _uiState.value = currentState.copy(title = title)
        }
    }

    fun updateDescription(description: String) {
        val currentState = _uiState.value
        if (currentState is UploadUiState.Ready && !currentState.isUploading) {
            Log.d(TAG, "Updating description to: $description")
            _uiState.value = currentState.copy(description = description)
        }
    }

    fun uploadVideo() {
        val currentState = _uiState.value
        if (currentState !is UploadUiState.Ready || currentState.isUploading) {
            Log.w(TAG, "Cannot start upload: Invalid state or already uploading")
            return
        }

        if (currentState.title.isBlank()) {
            viewModelScope.launch {
                Log.w(TAG, "Cannot start upload: Title is required")
                _events.emit(UploadEvent.ShowError("Please enter a title for your video"))
            }
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Starting video upload")
            Log.d(TAG, "Title: ${currentState.title}")
            Log.d(TAG, "Description: ${currentState.description}")
            
            _uiState.value = currentState.copy(isUploading = true)
            
            try {
                val video = videoRepository.uploadVideo(
                    videoFile = currentState.file,
                    title = currentState.title,
                    description = currentState.description,
                    onProgress = { progress ->
                        _uiState.value = (_uiState.value as UploadUiState.Ready).copy(
                            progress = progress / 100f
                        )
                    }
                )
                
                if (video != null) {
                    Log.d(TAG, "Upload complete: ${video.id}")
                    _events.emit(UploadEvent.NavigateToFeed(video.id))
                } else {
                    Log.e(TAG, "Upload failed: Repository returned null")
                    _uiState.value = currentState.copy(isUploading = false)
                    _events.emit(UploadEvent.ShowError("Failed to upload video"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload error: ${e.message}", e)
                _uiState.value = currentState.copy(isUploading = false)
                _events.emit(UploadEvent.ShowError("Error uploading video: ${e.message}"))
            }
        }
    }

    fun cancelUpload() {
        viewModelScope.launch {
            Log.d(TAG, "Cancelling upload")
            _events.emit(UploadEvent.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "=== ViewModel being cleared, canceling upload job ===")
        uploadJob?.cancel()
    }
} 