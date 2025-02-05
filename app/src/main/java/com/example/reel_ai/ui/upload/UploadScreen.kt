package com.example.reel_ai.ui.upload

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reel_ai.ui.common.ErrorScreen
import java.io.File
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "UploadScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    file: File,
    initialTitle: String? = null,
    initialDescription: String? = null,
    onUploadComplete: () -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize ViewModel with file and metadata
    LaunchedEffect(file) {
        viewModel.initializeWithFile(file, initialTitle, initialDescription)
    }

    // Log state changes
    DisposableEffect(uiState) {
        Log.d(TAG, "=== UI State changed to: ${uiState::class.simpleName} ===")
        when (val state = uiState) {
            is UploadUiState.Ready -> {
                Log.d(TAG, "=== Ready state: isUploading=${state.isUploading}, progress=${state.progress} ===")
            }
            is UploadUiState.Error -> {
                Log.e(TAG, "=== Error state: ${state.message} ===")
            }
            is UploadUiState.Loading -> {
                Log.d(TAG, "=== Loading state ===")
            }
        }
        onDispose {
            Log.d(TAG, "=== Disposing state observer ===")
        }
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UploadEvent.UploadComplete -> onUploadComplete()
                is UploadEvent.NavigateBack -> onCancel()
                is UploadEvent.ShowError -> onError(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Video") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.cancelUpload() },
                        enabled = (uiState as? UploadUiState.Ready)?.isUploading?.not() ?: true
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel"
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
                is UploadUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UploadUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = { viewModel.cancelUpload() }
                    )
                }
                is UploadUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.isUploading) {
                            Text(
                                text = "Uploading video...",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinearProgressIndicator(
                                progress = state.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${(state.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "Ready to upload",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "File: ${state.file.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.updateTitle(it) },
                                label = { Text("Title") },
                                placeholder = { Text("Enter video title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = state.title.isBlank()
                            )
                            
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.updateDescription(it) },
                                label = { Text("Description") },
                                placeholder = { Text("Enter video description (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                            Button(
                                onClick = { viewModel.uploadVideo() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.title.isNotBlank()
                            ) {
                                Text("Start Upload")
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelUpload() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
} 