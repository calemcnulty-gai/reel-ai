package com.example.reel_ai.ui.preview

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "PreviewScreen"

@Composable
fun PreviewScreen(
    file: File,
    onNavigateToUpload: (File) -> Unit,
    onNavigateToEdit: (File) -> Unit,
    onNavigateToDiscard: (File) -> Unit,
    onShowError: (String) -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PreviewEvent.NavigateToUpload -> onNavigateToUpload(event.file)
                is PreviewEvent.NavigateToEdit -> onNavigateToEdit(event.file)
                is PreviewEvent.NavigateToDiscard -> onNavigateToDiscard(event.file)
                is PreviewEvent.ShowError -> onShowError(event.message)
                else -> { /* Handle other events */ }
            }
        }
    }

    // Initialize ViewModel with file
    LaunchedEffect(file) {
        viewModel.initializeWithFile(file)
    }

    // Lifecycle handling for ExoPlayer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "Lifecycle ON_PAUSE: Pausing ExoPlayer")
                    exoPlayer?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "Lifecycle ON_RESUME: Resuming ExoPlayer if playing")
                    if (uiState is PreviewUiState.Success && (uiState as PreviewUiState.Success).isPlaying) {
                        exoPlayer?.play()
                    }
                }
                else -> { /* Handle other lifecycle events */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PreviewUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is PreviewUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            is PreviewUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Video player
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                if (exoPlayer == null) {
                                    exoPlayer = ExoPlayer.Builder(context).build().apply {
                                        val mediaItem = androidx.media3.common.MediaItem.fromUri(
                                            Uri.fromFile(state.videoFile)
                                        )
                                        setMediaItem(mediaItem)
                                        prepare()
                                        playWhenReady = state.isPlaying
                                    }
                                }
                                player = exoPlayer
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        update = { playerView ->
                            exoPlayer?.playWhenReady = state.isPlaying
                            playerView.player = exoPlayer
                        }
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.onUploadClick(state.videoFile) }
                        ) {
                            Text("Upload")
                        }
                        Button(
                            onClick = { viewModel.onEditClick(state.videoFile) }
                        ) {
                            Text("Edit")
                        }
                        Button(
                            onClick = { viewModel.onDiscardClick(state.videoFile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Discard")
                        }
                    }
                }
            }
        }
    }
} 