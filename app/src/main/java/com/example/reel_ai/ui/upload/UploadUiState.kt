package com.example.reel_ai.ui.upload

import java.io.File

sealed interface UploadUiState {
    data object Loading : UploadUiState
    data class Error(val message: String) : UploadUiState
    data class Ready(
        val file: File,
        val title: String = "",
        val description: String = "",
        val isUploading: Boolean = false,
        val progress: Float = 0f
    ) : UploadUiState
} 