package com.example.reel_ai.ui.edit

import java.io.File

sealed class EditUiState {
    data object Loading : EditUiState()
    
    data class Error(
        val message: String,
        val file: File
    ) : EditUiState()
    
    data class Ready(
        val file: File,
        val title: String = "",
        val description: String = "",
        val isProcessing: Boolean = false,
        val progress: Float = 0f
    ) : EditUiState()
}

sealed interface EditEvent {
    data class NavigateToUpload(
        val file: File,
        val title: String,
        val description: String
    ) : EditEvent
    data class ShowError(val message: String) : EditEvent
    data object NavigateBack : EditEvent
} 