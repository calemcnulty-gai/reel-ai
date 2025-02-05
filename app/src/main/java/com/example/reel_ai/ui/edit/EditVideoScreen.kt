package com.example.reel_ai.ui.edit

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

private const val TAG = "EditVideoScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVideoScreen(
    file: File,
    onNavigateToUpload: (File, String, String) -> Unit,
    onCancel: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showErrorSnackbar by remember { mutableStateOf<String?>(null) }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditEvent.NavigateToUpload -> onNavigateToUpload(
                    event.file,
                    event.title,
                    event.description
                )
                is EditEvent.NavigateBack -> onCancel()
                is EditEvent.ShowError -> {
                    Log.e(TAG, "Edit error: ${event.message}")
                    showErrorSnackbar = event.message
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
                title = { Text("Edit Video") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.cancelEdit() },
                        enabled = (uiState as? EditUiState.Ready)?.isProcessing?.not() ?: true
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(SnackbarHostState()) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    dismissAction = {
                        TextButton(onClick = { showErrorSnackbar = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is EditUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EditUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = { viewModel.cancelEdit() },
                        action = { viewModel.retryEdit() }
                    )
                }
                is EditUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edit your video",
                            style = MaterialTheme.typography.headlineSmall
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

                        Text(
                            text = "Video file: ${state.file.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = { viewModel.processVideo() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.title.isNotBlank()
                        ) {
                            Text("Continue to Upload")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.cancelEdit() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
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