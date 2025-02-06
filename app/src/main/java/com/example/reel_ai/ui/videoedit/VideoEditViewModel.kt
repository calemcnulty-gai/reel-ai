package com.example.reel_ai.ui.videoedit

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.repository.VideoRepository
import com.example.reel_ai.domain.video.VideoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "VideoEditViewModel"

@HiltViewModel
class VideoEditViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val videoManager: VideoManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var videoId: String = checkNotNull(savedStateHandle["videoId"])

    private val _uiState = MutableStateFlow<VideoEditUiState>(VideoEditUiState.Loading)
    val uiState: StateFlow<VideoEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VideoEditEvent>()
    val events: SharedFlow<VideoEditEvent> = _events.asSharedFlow()

    private val _thumbnailSelectionState = MutableStateFlow<ThumbnailSelectionState>(ThumbnailSelectionState())
    val thumbnailSelectionState: StateFlow<ThumbnailSelectionState> = _thumbnailSelectionState.asStateFlow()

    init {
        loadVideo()
    }

    private fun loadVideo() {
        viewModelScope.launch {
            try {
                val video = videoRepository.getVideo(videoId)
                if (video != null) {
                    _uiState.value = VideoEditUiState.Ready(
                        title = video.title ?: "",
                        description = video.description ?: "",
                        currentThumbnailUrl = video.thumbnailUrl,
                        videoUrl = video.videoUrl,
                        isSaving = false
                    )
                } else {
                    _uiState.value = VideoEditUiState.Error("Video not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video: ${e.message}", e)
                _uiState.value = VideoEditUiState.Error(e.message ?: "Failed to load video")
            }
        }
    }

    fun updateTitle(title: String) {
        val currentState = _uiState.value as? VideoEditUiState.Ready ?: return
        _uiState.value = currentState.copy(title = title)
    }

    fun updateDescription(description: String) {
        val currentState = _uiState.value as? VideoEditUiState.Ready ?: return
        _uiState.value = currentState.copy(description = description)
    }

    fun selectThumbnail() {
        val currentState = _uiState.value as? VideoEditUiState.Ready ?: return
        
        // Clean up any existing frames
        cleanupFrames()
        
        viewModelScope.launch {
            try {
                _thumbnailSelectionState.value = ThumbnailSelectionState(isLoading = true)
                
                // Download video file
                val videoFile = videoManager.downloadVideo(currentState.videoUrl)
                if (videoFile == null) {
                    _events.emit(VideoEditEvent.ShowError("Failed to download video"))
                    _thumbnailSelectionState.value = ThumbnailSelectionState(isLoading = false)
                    return@launch
                }

                try {
                    // Extract frames
                    val frames = extractFrames(videoFile)
                    if (frames.isEmpty()) {
                        _events.emit(VideoEditEvent.ShowError("Failed to extract video frames"))
                        _thumbnailSelectionState.value = ThumbnailSelectionState(isLoading = false)
                        return@launch
                    }
                    
                    _thumbnailSelectionState.value = ThumbnailSelectionState(
                        isLoading = false,
                        videoUri = Uri.fromFile(videoFile),
                        frames = frames
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error extracting frames: ${e.message}", e)
                    _events.emit(VideoEditEvent.ShowError("Failed to extract video frames"))
                    _thumbnailSelectionState.value = ThumbnailSelectionState(isLoading = false)
                } finally {
                    // Clean up video file
                    videoFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during thumbnail selection: ${e.message}", e)
                _events.emit(VideoEditEvent.ShowError("Failed to prepare thumbnail selection"))
                _thumbnailSelectionState.value = ThumbnailSelectionState(isLoading = false)
            }
        }
    }

    private fun extractFrames(videoFile: File): List<Uri> {
        val retriever = MediaMetadataRetriever()
        val frames = mutableListOf<Uri>()
        
        try {
            retriever.setDataSource(videoFile.absolutePath)
            
            // Get video duration in microseconds
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            val durationUs = duration * 1000 // Convert to microseconds
            
            if (durationUs <= 0) {
                Log.e(TAG, "Invalid video duration: $durationUs")
                return emptyList()
            }
            
            // Extract 8 frames evenly spaced throughout the video
            val frameCount = 8
            val interval = durationUs / (frameCount + 1)
            
            for (i in 1..frameCount) {
                val timeUs = interval * i
                val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                
                if (bitmap != null) {
                    try {
                        // Save frame to temporary file
                        val frameFile = File(context.cacheDir, "frame_${System.currentTimeMillis()}_$i.jpg")
                        frameFile.outputStream().use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        frames.add(Uri.fromFile(frameFile))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving frame $i: ${e.message}", e)
                    } finally {
                        bitmap.recycle()
                    }
                } else {
                    Log.e(TAG, "Failed to extract frame at $timeUs")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in frame extraction: ${e.message}", e)
            return emptyList()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing retriever: ${e.message}", e)
            }
        }
        
        return frames
    }

    fun onThumbnailSelected(uri: Uri?) {
        if (uri == null) return
        
        viewModelScope.launch {
            try {
                val currentState = _uiState.value as? VideoEditUiState.Ready ?: return@launch
                _uiState.value = currentState.copy(isSaving = true)

                // Create a copy of the selected image
                val thumbnailFile = File(context.cacheDir, "new_thumbnail_${System.currentTimeMillis()}.jpg")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        thumbnailFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    } ?: throw Exception("Failed to read selected image")

                    val success = videoManager.uploadThumbnail(thumbnailFile, videoId)
                    if (success) {
                        loadVideo() // Reload video to get new thumbnail URL
                        _events.emit(VideoEditEvent.ShowMessage("Thumbnail updated successfully"))
                    } else {
                        throw Exception("Failed to upload thumbnail")
                    }
                } finally {
                    thumbnailFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating thumbnail: ${e.message}", e)
                _events.emit(VideoEditEvent.ShowError(e.message ?: "Failed to update thumbnail"))
            } finally {
                val currentState = _uiState.value as? VideoEditUiState.Ready
                if (currentState != null) {
                    _uiState.value = currentState.copy(isSaving = false)
                }
                // Clean up frames after thumbnail selection
                cleanupFrames()
            }
        }
    }

    private fun cleanupFrames() {
        viewModelScope.launch {
            _thumbnailSelectionState.value.frames.forEach { uri ->
                try {
                    File(uri.path ?: "").delete()
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning up frame file: ${e.message}")
                }
            }
            _thumbnailSelectionState.value = ThumbnailSelectionState()
        }
    }

    fun startCropping() {
        // TODO: Implement video cropping
        viewModelScope.launch {
            _events.emit(VideoEditEvent.ShowError("Video cropping coming soon"))
        }
    }

    fun saveChanges() {
        val currentState = _uiState.value as? VideoEditUiState.Ready ?: return
        if (currentState.isSaving) return

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isSaving = true)
                
                val success = videoRepository.updateVideo(
                    videoId = videoId,
                    title = currentState.title.takeIf { it.isNotBlank() },
                    description = currentState.description.takeIf { it.isNotBlank() }
                )

                if (success) {
                    _events.emit(VideoEditEvent.EditComplete)
                } else {
                    _uiState.value = currentState.copy(isSaving = false)
                    _events.emit(VideoEditEvent.ShowError("Failed to save changes"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving changes: ${e.message}", e)
                _uiState.value = currentState.copy(isSaving = false)
                _events.emit(VideoEditEvent.ShowError(e.message ?: "Failed to save changes"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupFrames()
    }
}

sealed interface VideoEditUiState {
    data object Loading : VideoEditUiState
    data class Error(val message: String) : VideoEditUiState
    data class Ready(
        val title: String,
        val description: String,
        val currentThumbnailUrl: String?,
        val videoUrl: String,
        val isSaving: Boolean
    ) : VideoEditUiState
}

data class ThumbnailSelectionState(
    val isLoading: Boolean = false,
    val videoUri: Uri? = null,
    val frames: List<Uri> = emptyList()
)

sealed interface VideoEditEvent {
    data class ShowError(val message: String) : VideoEditEvent
    data class ShowMessage(val message: String) : VideoEditEvent
    data object EditComplete : VideoEditEvent
} 