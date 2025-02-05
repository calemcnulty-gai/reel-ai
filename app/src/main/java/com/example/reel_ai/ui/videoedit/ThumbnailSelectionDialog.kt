package com.example.reel_ai.ui.videoedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailSelectionDialog(
    onDismiss: () -> Unit,
    onThumbnailSelected: (Uri?) -> Unit,
    currentThumbnailUrl: String?,
    thumbnailFrames: List<Uri>,
    isLoading: Boolean
) {
    var selectedThumbnail by remember { mutableStateOf<Uri?>(null) }
    var showImageError by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedThumbnail = uri
            showImageError = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Thumbnail") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Loading state
                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Extracting video frames...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // Current thumbnail preview
                    if (currentThumbnailUrl != null) {
                        Text(
                            text = "Current Thumbnail",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = currentThumbnailUrl,
                                contentDescription = "Current thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Text(
                        text = "Select New Thumbnail",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Video frames grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        // Custom upload option
                        item {
                            Card(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Upload custom thumbnail",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Custom",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Video frames
                        items(thumbnailFrames) { frame ->
                            Card(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedThumbnail == frame) 
                                            MaterialTheme.colorScheme.primary 
                                        else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable { selectedThumbnail = frame }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = frame,
                                        contentDescription = "Video frame",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    if (selectedThumbnail == frame) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showImageError) {
                        Text(
                            text = "Please select a thumbnail to continue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Help text
                    Text(
                        text = "Tip: Choose from video frames or upload your own image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedThumbnail != null) {
                        onThumbnailSelected(selectedThumbnail)
                        onDismiss()
                    } else {
                        showImageError = true
                    }
                },
                enabled = !isLoading
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
} 