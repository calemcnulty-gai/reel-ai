package com.example.reel_ai.ui.videoedit

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.reel_ai.ui.common.ErrorScreen
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "VideoEditScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: VideoEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val thumbnailState by viewModel.thumbnailSelectionState.collectAsState()
    var showErrorSnackbar by remember { mutableStateOf<String?>(null) }
    var showThumbnailDialog by remember { mutableStateOf(false) }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is VideoEditEvent.ShowError -> {
                    showErrorSnackbar = event.message
                }
                is VideoEditEvent.ShowMessage -> {
                    showErrorSnackbar = event.message
                }
                is VideoEditEvent.EditComplete -> {
                    onNavigateBack()
                }
            }
        }
    }

    // Show thumbnail selection dialog if needed
    if (showThumbnailDialog) {
        val currentState = uiState as? VideoEditUiState.Ready
        ThumbnailSelectionDialog(
            onDismiss = { showThumbnailDialog = false },
            onThumbnailSelected = { uri ->
                viewModel.onThumbnailSelected(uri)
                showThumbnailDialog = false
            },
            currentThumbnailUrl = currentState?.currentThumbnailUrl,
            thumbnailFrames = thumbnailState.frames,
            isLoading = thumbnailState.isLoading
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Video") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState is VideoEditUiState.Ready) {
                        TextButton(
                            onClick = { viewModel.saveChanges() },
                            enabled = !(uiState as VideoEditUiState.Ready).isSaving
                        ) {
                            Text("Save")
                        }
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
                is VideoEditUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VideoEditUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = onNavigateBack
                    )
                }
                is VideoEditUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Video Preview Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f/9f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = state.currentThumbnailUrl,
                                    contentDescription = "Video thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Thumbnail selection button
                                Button(
                                    onClick = { 
                                        viewModel.selectThumbnail()
                                        showThumbnailDialog = true
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Change Thumbnail")
                                }
                            }
                        }

                        // Video Details Section
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        // Video Editing Section
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Video Editing",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                // Crop Video Button
                                OutlinedButton(
                                    onClick = { viewModel.startCropping() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Crop,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crop Video")
                                }
                            }
                        }

                        if (state.isSaving) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Show error snackbar if needed
            showErrorSnackbar?.let { error ->
                LaunchedEffect(error) {
                    SnackbarHostState().showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Short
                    )
                    showErrorSnackbar = null
                }
            }
        }
    }
} 