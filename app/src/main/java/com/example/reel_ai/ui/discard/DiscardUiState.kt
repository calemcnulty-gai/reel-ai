package com.example.reel_ai.ui.discard

import java.io.File

sealed interface DiscardUiState {
    data object Loading : DiscardUiState
    data class Error(val message: String) : DiscardUiState
    data class Ready(
        val file: File,
        val isDeleting: Boolean = false
    ) : DiscardUiState
}

sealed interface DiscardEvent {
    data object NavigateToFeed : DiscardEvent
    data class ShowError(val message: String) : DiscardEvent
    data object NavigateBack : DiscardEvent
} 