package com.example.reel_ai.ui.profile

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.repository.VideoRepository
import com.example.reel_ai.domain.auth.AuthRepository
import com.example.reel_ai.domain.model.User
import com.example.reel_ai.domain.model.Video
import com.example.reel_ai.data.service.ThumbnailGenerationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val videoRepository: VideoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                if (!authRepository.isUserSignedIn()) {
                    Log.e(TAG, "No user logged in")
                    _uiState.value = ProfileUiState.Error("User not logged in")
                    return@launch
                }

                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Log.e(TAG, "Failed to get user info")
                    _uiState.value = ProfileUiState.Error("Failed to get user info")
                    return@launch
                }

                Log.d(TAG, "Loading videos for user: ${currentUser.id}")
                videoRepository.getUserVideos()
                    .catch { e ->
                        Log.e(TAG, "Error collecting videos: ${e.message}", e)
                        _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load videos")
                        _events.emit(ProfileEvent.ShowError(e.message ?: "Failed to load videos"))
                    }
                    .collect { videos ->
                        Log.d(TAG, "Loaded ${videos.size} videos")
                        _uiState.value = ProfileUiState.Ready(
                            user = currentUser,
                            videos = videos
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile: ${e.message}", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
                _events.emit(ProfileEvent.ShowError(e.message ?: "Failed to load profile"))
            }
        }
    }

    fun updateProfile(displayName: String, handle: String, bio: String) {
        viewModelScope.launch {
            try {
                // TODO: Update user profile in Firebase Auth
                Log.d(TAG, "Updating profile - name: $displayName, handle: $handle, bio: $bio")
                
                // For now, just update the local state
                val currentState = _uiState.value as? ProfileUiState.Ready ?: return@launch
                _uiState.value = currentState.copy(
                    user = currentState.user.copy(
                        displayName = displayName.takeIf { it.isNotBlank() },
                        handle = handle.takeIf { it.isNotBlank() },
                        bio = bio.takeIf { it.isNotBlank() }
                    )
                )
                
                _events.emit(ProfileEvent.ProfileUpdated)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile: ${e.message}", e)
                _events.emit(ProfileEvent.ShowError(e.message ?: "Failed to update profile"))
            }
        }
    }

    fun refreshProfile() {
        Log.d(TAG, "Refreshing profile")
        loadUserProfile()
    }

    fun generateMissingThumbnails() {
        val serviceIntent = Intent(context, ThumbnailGenerationService::class.java).apply {
            action = ThumbnailGenerationService.ACTION_START
        }
        context.startService(serviceIntent)
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Ready(
        val user: User,
        val videos: List<Video>
    ) : ProfileUiState
}

sealed interface ProfileEvent {
    data class ShowError(val message: String) : ProfileEvent
    data object ProfileUpdated : ProfileEvent
    data class ShowMessage(val message: String) : ProfileEvent
} 