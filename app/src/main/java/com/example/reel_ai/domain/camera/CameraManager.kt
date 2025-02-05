package com.example.reel_ai.domain.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor() {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView
    ) {
        try {
            Log.d("ReelAI", "CameraManager: Initializing camera with selector: $currentCameraSelector")
            cameraProvider = ProcessCameraProvider.getInstance(context).get()
            
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            
            videoCapture = VideoCapture.withOutput(recorder)
            
            val cameraSelector = currentCameraSelector
            
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            Log.d("ReelAI", "CameraManager: Camera bound successfully to lifecycle with selector: $cameraSelector")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            _recordingState.value = RecordingState.Error("Failed to initialize camera: ${e.message}")
        }
    }
    
    fun startRecording(context: Context, outputFile: File) {
        val videoCapture = videoCapture ?: run {
            Log.e("ReelAI", "CameraManager: videoCapture is null in startRecording")
            return
        }
        
        val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()
        
        Log.d("ReelAI", "CameraManager: Starting recording with output file: ${outputFile.absolutePath}")
        
        recording = videoCapture.output
            .prepareRecording(context, fileOutputOptions)
            .start(Runnable::run) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        _recordingState.value = RecordingState.Recording(System.currentTimeMillis())
                        Log.d("ReelAI", "CameraManager: Recording started at ${System.currentTimeMillis()}")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.hasError()) {
                            _recordingState.value = RecordingState.Error("Video capture failed: ${recordEvent.error}")
                            Log.e("ReelAI", "CameraManager: Video capture failed: ${recordEvent.error}")
                            outputFile.delete()
                        } else {
                            _recordingState.value = RecordingState.Success(outputFile)
                            Log.d("ReelAI", "CameraManager: Recording finalized successfully, file: ${outputFile.absolutePath}")
                        }
                    }
                }
            }
    }
    
    fun stopRecording() {
        recording?.stop()
        Log.d("ReelAI", "CameraManager: Stopping recording")
        recording = null
    }
    
    fun release() {
        try {
            Log.d("ReelAI", "CameraManager: Releasing camera resources")
            cameraProvider?.unbindAll()
            cameraProvider = null
            videoCapture = null
            recording?.stop()
            recording = null
            Log.d("ReelAI", "CameraManager: Camera resources released")
        } catch (e: Exception) {
            Log.e("ReelAI", "CameraManager: Error releasing camera resources", e)
        }
    }
    
    fun toggleCamera() {
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        Log.d("ReelAI", "CameraManager: Switched camera, currentCameraSelector: $currentCameraSelector")
    }
    
    fun resetRecordingState() {
        _recordingState.value = RecordingState.Idle
    }
    
    companion object {
        private const val TAG = "CameraManager"
        const val MIN_RECORDING_TIME_MS = 5000L // 5 seconds
        const val MAX_RECORDING_TIME_MS = 60000L // 1 minute
    }
}

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val durationMs: Long = 0L) : RecordingState()
    data class Success(val outputFile: File) : RecordingState()
    data class Error(val message: String) : RecordingState()
} 