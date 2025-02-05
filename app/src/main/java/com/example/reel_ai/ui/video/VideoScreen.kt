package com.example.reel_ai.ui.video

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reel_ai.ui.common.ErrorScreen
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "VideoScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    videoId: String,
    onNavigateBack: () -> Unit,
    onError: (String) -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize ViewModel with video ID
    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is VideoEvent.ShowError -> {
                    Log.e(TAG, "Video error: ${event.message}")
                    onError(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.shareVideo() },
                        enabled = uiState is VideoUiState.Ready
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is VideoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VideoUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = onNavigateBack
                    )
                }
                is VideoUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Video player
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f/9f)
                        ) {
                            // TODO: Implement video player
                            Text(
                                text = "Video Player Placeholder",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Video info
                        Text(
                            text = state.video.title ?: "Untitled Video",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        state.video.description?.let { description ->
                            if (description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "${state.video.viewCount} views",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${state.video.shareCount} shares",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
} 