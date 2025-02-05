package com.example.reel_ai.ui.preview

import java.io.File

sealed interface PreviewUiState {
    data object Loading : PreviewUiState
    data class Error(val message: String) : PreviewUiState
    data class Success(
        val videoFile: File,
        val isPlaying: Boolean = true
    ) : PreviewUiState
}

sealed interface PreviewEvent {
    data object NavigateToFeed : PreviewEvent
    data class ShowError(val message: String) : PreviewEvent
    data class NavigateToUpload(val file: File) : PreviewEvent
    data class NavigateToEdit(val file: File) : PreviewEvent
    data class NavigateToDiscard(val file: File) : PreviewEvent
} 