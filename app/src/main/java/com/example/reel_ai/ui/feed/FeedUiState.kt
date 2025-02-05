package com.example.reel_ai.ui.feed

import com.example.reel_ai.domain.model.Video

data class FeedUiState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 