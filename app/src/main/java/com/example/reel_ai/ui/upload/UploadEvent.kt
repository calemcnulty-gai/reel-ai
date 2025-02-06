package com.example.reel_ai.ui.upload

sealed class UploadEvent {
    data object UploadComplete : UploadEvent()
    data class ShowError(val message: String) : UploadEvent()
    data object NavigateBack : UploadEvent()
    data class NavigateToFeed(val videoId: String) : UploadEvent()
} 