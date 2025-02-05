package com.example.reel_ai.ui.upload

sealed interface UploadEvent {
    data class ShowError(val message: String) : UploadEvent
    data object NavigateBack : UploadEvent
    data object UploadComplete : UploadEvent
} 