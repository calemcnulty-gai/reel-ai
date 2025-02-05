package com.example.reel_ai.ui.camera

import android.content.Context
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.camera.CameraManager
import com.example.reel_ai.domain.camera.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {
    
    val recordingState: StateFlow<RecordingState> = cameraManager.recordingState
    private var previewView: PreviewView? = null
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private var timerJob: Job? = null
    private var recordingStartTime: Long = 0L
    
    private var savedContext: Context? = null
    private var savedLifecycleOwner: LifecycleOwner? = null
    
    fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        savedContext = context
        savedLifecycleOwner = lifecycleOwner
        viewModelScope.launch {
            previewView?.let { preview ->
                cameraManager.initializeCamera(context, lifecycleOwner, preview)
            }
        }
    }
    
    fun updatePreviewView(preview: PreviewView) {
        previewView = preview
    }
    
    fun startRecording(context: Context) {
        val outputFile = File(
            context.getExternalFilesDir(null),
            "recording_${System.currentTimeMillis()}.mp4"
        )
        recordingStartTime = System.currentTimeMillis()
        cameraManager.startRecording(context, outputFile)
        startTimer()
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - recordingStartTime
                _elapsedTime.value = elapsed
                
                if (elapsed >= CameraManager.MAX_RECORDING_TIME_MS) {
                    stopRecording()
                    break
                }
                delay(100) // Update every 100ms
            }
        }
    }
    
    fun stopRecording() {
        if (_elapsedTime.value >= CameraManager.MIN_RECORDING_TIME_MS) {
            timerJob?.cancel()
            timerJob = null
            _elapsedTime.value = 0L
            cameraManager.stopRecording()
        }
    }
    
    fun canStopRecording(): Boolean {
        return _elapsedTime.value >= CameraManager.MIN_RECORDING_TIME_MS
    }
    
    fun releaseCamera() {
        timerJob?.cancel()
        timerJob = null
        _elapsedTime.value = 0L
        cameraManager.release()
    }
    
    fun toggleCamera() {
        Log.d("ReelAI", "CameraViewModel: Toggling camera, calling CameraManager.toggleCamera()")
        cameraManager.toggleCamera()
        savedContext?.let { context ->
            savedLifecycleOwner?.let { owner ->
                previewView?.let { preview ->
                    viewModelScope.launch {
                        Log.d("ReelAI", "CameraViewModel: Reinitializing camera with context, owner, and preview")
                        cameraManager.initializeCamera(context, owner, preview)
                        Log.d("ReelAI", "CameraViewModel: Camera reinitialized successfully")
                    }
                }
            }
        }
    }
    
    fun resetRecordingState() {
        cameraManager.resetRecordingState()
    }
    
    override fun onCleared() {
        super.onCleared()
        releaseCamera()
    }
} 