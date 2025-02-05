package com.example.reel_ai.util

import android.content.Context
import com.example.reel_ai.domain.repository.VideoRepository
import java.io.File
import javax.inject.Inject

class VideoUploader @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend fun uploadTestVideo(context: Context) {
        val testVideo = File(context.filesDir, "phil.mp4")
        if (!testVideo.exists()) {
            // Copy from assets to internal storage
            context.assets.open("media/phil.mp4").use { input ->
                testVideo.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        
        videoRepository.uploadVideo(
            videoFile = testVideo,
            title = "Test Video",
            description = "A test video upload"
        )?.let {
            println("Successfully uploaded video with ID: ${it.id}")
        } ?: println("Failed to upload video")
    }
} 