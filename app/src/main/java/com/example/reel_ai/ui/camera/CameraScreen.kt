package com.example.reel_ai.ui.camera

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.example.reel_ai.domain.camera.CameraManager
import com.example.reel_ai.domain.camera.RecordingState
import com.example.reel_ai.ui.common.ErrorScreen
import java.io.File
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Cached
import androidx.compose.ui.graphics.Color
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateToPreview: (File) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val recordingState by viewModel.recordingState.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    
    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status is PermissionStatus.Granted) {
            viewModel.initializeCamera(context, lifecycleOwner)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.releaseCamera()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status is PermissionStatus.Granted) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    viewModel.updatePreviewView(previewView)
                }
            )
            
            IconButton(
                onClick = { viewModel.toggleCamera() },
                enabled = (recordingState !is RecordingState.Recording),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cached,
                    contentDescription = "Toggle Camera",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Timer display
            if (recordingState is RecordingState.Recording) {
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (recordingState) {
                    is RecordingState.Recording -> {
                        val canStop = viewModel.canStopRecording()
                        val remainingTime = if (!canStop) {
                            ((CameraManager.MIN_RECORDING_TIME_MS - elapsedTime) / 1000 + 1).coerceAtLeast(0)
                        } else {
                            ((CameraManager.MAX_RECORDING_TIME_MS - elapsedTime) / 1000).coerceAtLeast(0)
                        }
                        
                        Text(
                            text = if (!canStop) "Wait ${remainingTime}s" else "$remainingTime seconds remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        
                        Button(
                            onClick = { viewModel.stopRecording() },
                            enabled = canStop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("Stop Recording")
                        }
                    }
                    is RecordingState.Success -> {
                        LaunchedEffect(Unit) {
                            onNavigateToPreview((recordingState as RecordingState.Success).outputFile)
                        }
                    }
                    is RecordingState.Error -> {
                        ErrorScreen(
                            message = (recordingState as RecordingState.Error).message,
                            onDismiss = { viewModel.resetRecordingState() },
                            action = { viewModel.startRecording(context) }
                        )
                    }
                    else -> {
                        Button(
                            onClick = { viewModel.startRecording(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = "Record"
                                )
                                Text("Start Recording")
                            }
                        }
                    }
                }
            }
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val duration = timeMs.milliseconds
    val seconds = duration.inWholeSeconds
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
} 