package com.example.reel_ai.ui.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reel_ai.ui.common.ErrorScreen
import com.example.reel_ai.ui.common.VideoThumbnail
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "ProfileScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToEditVideo: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showErrorSnackbar by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedDisplayName by remember { mutableStateOf("") }
    var editedHandle by remember { mutableStateOf("") }
    var editedBio by remember { mutableStateOf("") }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowError -> {
                    Log.e(TAG, "Profile error: ${event.message}")
                    showErrorSnackbar = event.message
                }
                is ProfileEvent.ShowMessage -> {
                    showErrorSnackbar = event.message
                }
                is ProfileEvent.ProfileUpdated -> {
                    isEditing = false
                    showErrorSnackbar = "Profile updated successfully"
                }
            }
        }
    }

    // Initialize edit fields when entering edit mode
    LaunchedEffect(isEditing, uiState) {
        if (isEditing && uiState is ProfileUiState.Ready) {
            val user = (uiState as ProfileUiState.Ready).user
            editedDisplayName = user.displayName ?: ""
            editedHandle = user.handle ?: ""
            editedBio = user.bio ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState is ProfileUiState.Ready) {
                        if (isEditing) {
                            TextButton(
                                onClick = {
                                    viewModel.updateProfile(
                                        displayName = editedDisplayName,
                                        handle = editedHandle,
                                        bio = editedBio
                                    )
                                }
                            ) {
                                Text("Save")
                            }
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile"
                                )
                            }
                        }
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
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProfileUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onDismiss = onNavigateBack
                    )
                }
                is ProfileUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // User info section
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedDisplayName,
                                onValueChange = { editedDisplayName = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editedHandle,
                                onValueChange = { editedHandle = it },
                                label = { Text("Handle") },
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("@") }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editedBio,
                                onValueChange = { editedBio = it },
                                label = { Text("Bio") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        } else {
                            Column {
                                Text(
                                    text = state.user.displayName ?: "Anonymous",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                
                                state.user.handle?.let { handle ->
                                    Text(
                                        text = "@$handle",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                state.user.bio?.let { bio ->
                                    if (bio.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = bio,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${state.videos.size} videos",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Admin actions for thumbnail generation
                        Button(
                            onClick = { viewModel.generateMissingThumbnails() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Missing Thumbnails")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Videos Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.videos) { video ->
                                VideoThumbnail(
                                    video = video,
                                    onClick = { onNavigateToVideo(video.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    showEditButton = true,
                                    onEditClick = { onNavigateToEditVideo(video.id) }
                                )
                            }
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