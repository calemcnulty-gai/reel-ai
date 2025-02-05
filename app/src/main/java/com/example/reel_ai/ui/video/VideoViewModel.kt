package com.example.reel_ai.ui.video

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.repository.VideoRepository
import com.example.reel_ai.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VideoViewModel"

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Loading)
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VideoEvent>()
    val events: SharedFlow<VideoEvent> = _events.asSharedFlow()

    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading video: $videoId")
                val video = videoRepository.getVideo(videoId)
                if (video != null) {
                    Log.d(TAG, "Video loaded successfully")
                    _uiState.value = VideoUiState.Ready(video)
                    // Increment view count
                    videoRepository.incrementViewCount(videoId)
                } else {
                    Log.e(TAG, "Video not found")
                    _uiState.value = VideoUiState.Error("Video not found")
                    _events.emit(VideoEvent.ShowError("Video not found"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video: ${e.message}", e)
                _uiState.value = VideoUiState.Error(e.message ?: "Failed to load video")
                _events.emit(VideoEvent.ShowError(e.message ?: "Failed to load video"))
            }
        }
    }

    fun shareVideo() {
        val currentState = _uiState.value
        if (currentState !is VideoUiState.Ready) {
            Log.w(TAG, "Cannot share video: Invalid state")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Sharing video: ${currentState.video.id}")
                videoRepository.incrementShareCount(currentState.video.id)
                // TODO: Implement actual sharing functionality
                _events.emit(VideoEvent.ShowError("Sharing not implemented yet"))
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing video: ${e.message}", e)
                _events.emit(VideoEvent.ShowError("Failed to share video: ${e.message}"))
            }
        }
    }
}

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data class Error(val message: String) : VideoUiState
    data class Ready(val video: Video) : VideoUiState
}

sealed interface VideoEvent {
    data class ShowError(val message: String) : VideoEvent
} 