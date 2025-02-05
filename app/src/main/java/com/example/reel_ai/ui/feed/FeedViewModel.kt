package com.example.reel_ai.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.model.Video
import com.example.reel_ai.domain.repository.VideoRepository
import com.example.reel_ai.util.VideoUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "ReelAI_FeedViewModel"

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val videoUploader: VideoUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentVideoId: String? = null

    init {
        loadVideos()
    }

    fun uploadTestVideo(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Starting test video upload process ===")
                // Copy test video from assets to app's files directory
                val targetFile = File(context.filesDir, "phil.mp4")
                
                if (!targetFile.exists()) {
                    Log.d(TAG, "=== Test video not found in files directory, copying from assets ===")
                    try {
                        context.assets.open("media/phil.mp4").use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.d(TAG, "=== Successfully copied test video to ${targetFile.absolutePath} ===")
                    } catch (e: Exception) {
                        Log.e(TAG, "=== Failed to copy test video from assets: ${e.message} ===", e)
                        _uiState.update { it.copy(
                            error = "Failed to copy test video: ${e.message}"
                        )}
                        return@launch
                    }
                } else {
                    Log.d(TAG, "=== Test video already exists at ${targetFile.absolutePath} ===")
                }
                
                Log.d(TAG, "=== Uploading video to repository ===")
                val video = videoRepository.uploadVideo(
                    videoFile = targetFile,
                    title = "Test Video",
                    description = "A test video upload"
                )
                
                if (video != null) {
                    Log.d(TAG, "=== Successfully uploaded video with ID: ${video.id} ===")
                    _uiState.update { state ->
                        state.copy(
                            videos = listOf(video) + state.videos,
                            error = null
                        )
                    }
                } else {
                    Log.e(TAG, "=== Failed to upload test video: Repository returned null ===")
                    _uiState.update { it.copy(
                        error = "Failed to upload test video"
                    )}
                }
            } catch (e: Exception) {
                Log.e(TAG, "=== Error uploading test video: ${e.message} ===", e)
                _uiState.update { it.copy(
                    error = "Error uploading test video: ${e.message}"
                )}
            }
        }
    }

    fun loadVideos(refresh: Boolean = false) {
        if (_uiState.value.isLoading) {
            Log.d(TAG, "=== Skipping video load - already loading ===")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "=== Loading videos (refresh=$refresh) ===")
            _uiState.update { it.copy(isLoading = true) }

            try {
                val startAfter = if (refresh) null else _uiState.value.videos.lastOrNull()?.id
                Log.d(TAG, "=== Starting video fetch, startAfter=$startAfter ===")
                videoRepository.getVideos(limit = 10, startAfter = startAfter)
                    .distinctUntilChanged()
                    .collect { videos ->
                        Log.d(TAG, "=== Received ${videos.size} videos from repository ===")
                        _uiState.update { state ->
                            // If refreshing, use new list. Otherwise, ensure we don't add duplicates
                            val updatedVideos = if (refresh) {
                                videos
                            } else {
                                val existingIds = state.videos.map { it.id }.toSet()
                                state.videos + videos.filter { !existingIds.contains(it.id) }
                            }
                            state.copy(
                                videos = updatedVideos,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "=== Failed to load videos: ${e.message} ===", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load videos: ${e.message}"
                )}
            }
        }
    }

    fun onVideoVisible(video: Video) {
        if (currentVideoId != video.id) {
            currentVideoId = video.id
            Log.d(TAG, "=== Video became visible: ${video.id} ===")
            viewModelScope.launch {
                try {
                    videoRepository.incrementViewCount(video.id)
                    Log.d(TAG, "=== Successfully incremented views for video: ${video.id} ===")
                } catch (e: Exception) {
                    Log.e(TAG, "=== Failed to increment views: ${e.message} ===", e)
                }
            }
        }
    }

    fun toggleLike(video: Video) {
        viewModelScope.launch {
            Log.d(TAG, "=== Toggling like for video: ${video.id} ===")
            try {
                val isLiked = videoRepository.toggleLike(video.id)
                Log.d(TAG, "=== Successfully toggled like (isLiked=$isLiked) ===")
                // Update local state immediately for better UX
                _uiState.update { state ->
                    val updatedVideos = state.videos.map { v ->
                        if (v.id == video.id) {
                            v.copy(likes = v.likes + if (isLiked) 1 else -1)
                        } else v
                    }
                    state.copy(videos = updatedVideos)
                }
            } catch (e: Exception) {
                Log.e(TAG, "=== Failed to toggle like: ${e.message} ===", e)
            }
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            Log.d(TAG, "=== Attempting to delete video: ${video.id} ===")
            try {
                val success = videoRepository.deleteVideo(video.id)
                if (success) {
                    Log.d(TAG, "=== Successfully deleted video: ${video.id} ===")
                    _uiState.update { state ->
                        state.copy(
                            videos = state.videos.filter { it.id != video.id }
                        )
                    }
                } else {
                    Log.e(TAG, "=== Failed to delete video: ${video.id} ===")
                }
            } catch (e: Exception) {
                Log.e(TAG, "=== Error deleting video: ${e.message} ===", e)
            }
        }
    }

    fun updateError(message: String?) {
        _uiState.update { it.copy(error = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 