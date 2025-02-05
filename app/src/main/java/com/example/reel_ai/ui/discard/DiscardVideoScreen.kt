package com.example.reel_ai.ui.discard

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

private const val TAG = "DiscardVideoScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscardVideoScreen(
    file: File,
    onDiscardConfirm: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DiscardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DiscardEvent.NavigateToFeed -> onDiscardConfirm()
                is DiscardEvent.NavigateBack -> onCancel()
                is DiscardEvent.ShowError -> {
                    Log.e(TAG, "Discard error: ${event.message}")
                    // TODO: Show snackbar or other error UI
                }
            }
        }
    }

    // Initialize ViewModel with file
    LaunchedEffect(file) {
        viewModel.initializeWithFile(file)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discard Video") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.cancelDiscard() },
                        enabled = (uiState as? DiscardUiState.Ready)?.isDeleting?.not() ?: true
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
                is DiscardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DiscardUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = { viewModel.cancelDiscard() }
                        // No retry action needed for discard operation
                    )
                }
                is DiscardUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Are you sure you want to discard this video?",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Text(
                            text = "This action cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Text(
                            text = "Video file: ${state.file.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (state.isDeleting) {
                            CircularProgressIndicator()
                            Text(
                                text = "Deleting video...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Button(
                                onClick = { viewModel.confirmDiscard() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Yes, Discard Video")
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelDiscard() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("No, Keep Video")
                            }
                        }
                    }
                }
            }
        }
    }
} 