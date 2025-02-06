package com.example.reel_ai.ui.feed

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.reel_ai.domain.model.Video
import com.example.reel_ai.ui.common.ErrorScreen
import kotlinx.coroutines.delay
import com.example.reel_ai.ui.components.UserAvatar
import androidx.compose.ui.zIndex

private const val TAG = "ReelAI_FeedScreen"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit,
    targetVideoId: String? = null,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Reverse the videos list for display
    val displayVideos = remember(uiState.videos) { uiState.videos.reversed() }
    val pagerState = rememberPagerState(pageCount = { displayVideos.size })
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a map of ExoPlayers and their play states for each page
    val players = remember {
        mutableMapOf<Int, ExoPlayer>()
    }
    val playStates = remember {
        mutableMapOf<Int, MutableState<Boolean>>()
    }

    // Cleanup players when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            players.values.forEach { player ->
                player.release()
            }
            players.clear()
            playStates.clear()
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            players.values.forEach { player ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        player.pause()
                        Log.d(TAG, "=== App paused, pausing video ===")
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        val page = pagerState.currentPage
                        if (playStates[page]?.value == true && player.playbackState == Player.STATE_READY) {
                            player.play()
                            Log.d(TAG, "=== App resumed, resuming video ===")
                        }
                    }
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Update the LaunchedEffect for target video to use the reversed list
    LaunchedEffect(displayVideos, targetVideoId) {
        if (targetVideoId != null && displayVideos.isNotEmpty()) {
            val targetIndex = displayVideos.indexOfFirst { it.id == targetVideoId }
            if (targetIndex != -1) {
                delay(100)
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Add UserAvatar at the top
        UserAvatar(
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToUpload = onNavigateToCamera,
            onSignOut = onSignOut,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .zIndex(1f)
        )

        if (displayVideos.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No videos yet",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Button(
                    onClick = { viewModel.uploadTestVideo(context) }
                ) {
                    Text("Upload Test Video")
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = displayVideos.getOrNull(page) ?: return@VerticalPager

                // Create or get player and play state for this page
                val player = remember(page) {
                    players.getOrPut(page) {
                        ExoPlayer.Builder(context).build().apply {
                            repeatMode = Player.REPEAT_MODE_ONE
                            volume = 1f
                            setMediaItem(MediaItem.fromUri(Uri.parse(video.videoUrl)))
                            prepare()
                        }
                    }
                }
                val isPlaying = remember(page) {
                    playStates.getOrPut(page) { mutableStateOf(true) }
                }

                // Handle player visibility
                LaunchedEffect(pagerState.currentPage) {
                    if (page == pagerState.currentPage) {
                        viewModel.onVideoVisible(video)
                        if (isPlaying.value) {
                            player.play()
                            Log.d(TAG, "=== Video became visible, playing ===")
                        }
                    } else {
                        player.pause()
                        Log.d(TAG, "=== Video no longer visible, pausing ===")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable {
                            isPlaying.value = !isPlaying.value
                            if (isPlaying.value) {
                                player.play()
                                Log.d(TAG, "=== User tapped video, playing ===")
                            } else {
                                player.pause()
                                Log.d(TAG, "=== User tapped video, pausing ===")
                            }
                        }
                ) {
                    // Video Player
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                this.player = player
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Play/Pause Indicator
                    if (!isPlaying.value) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Video Info Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        video.title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                        video.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Interaction Buttons
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = { viewModel.toggleLike(video) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "${video.likes}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${video.views} views",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error message
        uiState.error?.let { error ->
            ErrorScreen(
                message = error,
                onDismiss = { viewModel.clearError() },
                // No action needed here as users can just return to feed
            )
        }

        // FAB for recording new video
        FloatingActionButton(
            onClick = onNavigateToCamera,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Record new video"
            )
        }
    }
} 