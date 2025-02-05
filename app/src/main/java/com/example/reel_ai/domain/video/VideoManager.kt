package com.example.reel_ai.domain.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class VideoManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "VideoManager"
        private const val MAX_CACHE_SIZE = 500 * 1024 * 1024L // 500MB cache
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, "media").also { 
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }

    private val databaseProvider by lazy {
        StandaloneDatabaseProvider(context)
    }

    private val cache: Cache by lazy {
        SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
            databaseProvider
        )
    }

    private val cacheDataSourceFactory by lazy {
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            .setCacheWriteDataSinkFactory(null) // Disable writing to cache for local files
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * Creates and configures an ExoPlayer instance with caching support
     * @param file The video file to play
     * @param autoPlay Whether the video should start playing automatically
     * @return Configured ExoPlayer instance
     */
    fun createPlayer(file: File, autoPlay: Boolean = true): ExoPlayer {
        Log.d(TAG, "Creating ExoPlayer for file: ${file.absolutePath}")
        
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(cacheDataSourceFactory)
            )
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = autoPlay
                Log.d(TAG, "ExoPlayer configured with caching support")
            }
    }

    /**
     * Safely releases an ExoPlayer instance
     * @param player The ExoPlayer instance to release
     */
    fun releasePlayer(player: ExoPlayer) {
        Log.d(TAG, "Releasing ExoPlayer")
        player.release()
    }

    /**
     * Deletes a video file
     * @param file The video file to delete
     * @return true if deletion was successful, false otherwise
     */
    fun deleteVideo(file: File): Boolean {
        return try {
            val success = file.delete()
            Log.d(TAG, "Video deletion ${if (success) "successful" else "failed"}: ${file.absolutePath}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video file: ${file.absolutePath}", e)
            false
        }
    }

    /**
     * Validates if a video file exists and is readable
     * @param file The video file to validate
     * @return true if the file is valid, false otherwise
     */
    fun validateVideo(file: File): Boolean {
        if (!file.exists() || !file.canRead()) {
            Log.e(TAG, "Video file validation failed: exists=${file.exists()}, canRead=${file.canRead()}")
            return false
        }
        return true
    }

    /**
     * Clears the video cache
     */
    fun clearCache() {
        try {
            Log.d(TAG, "Clearing video cache")
            cache.release()
            cacheDir.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    fun generateThumbnail(videoFile: File): File? {
        try {
            Log.d(TAG, "Generating thumbnail for video: ${videoFile.absolutePath}")
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(videoFile))
            
            // Get a frame from 1 second into the video
            val bitmap = retriever.getFrameAtTime(
                1000000, // 1 second in microseconds
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to extract frame from video")
                return null
            }
            
            // Create a file for the thumbnail
            val thumbnailFile = File(
                context.cacheDir,
                "thumb_${System.currentTimeMillis()}.jpg"
            )
            
            // Save the bitmap to the file
            thumbnailFile.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            bitmap.recycle()
            retriever.release()
            
            Log.d(TAG, "Successfully generated thumbnail: ${thumbnailFile.absolutePath}")
            return thumbnailFile
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail: ${e.message}", e)
            return null
        }
    }

    suspend fun downloadVideo(videoUrl: String): File? {
        return try {
            Log.d(TAG, "=== [DOWNLOAD] Starting video download from: $videoUrl ===")
            
            val videoFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.mp4")
            val videoRef = storage.getReferenceFromUrl(videoUrl)
            
            withTimeout(60000) { // 60 second timeout
                videoRef.getFile(videoFile).await()
            }
            
            Log.d(TAG, "=== [DOWNLOAD] Video downloaded successfully to: ${videoFile.absolutePath} ===")
            videoFile
        } catch (e: Exception) {
            Log.e(TAG, "=== [DOWNLOAD] Error downloading video: ${e.message} ===", e)
            null
        }
    }

    suspend fun uploadThumbnail(thumbnailFile: File, videoId: String): Boolean {
        return try {
            Log.d(TAG, "=== [UPLOAD-THUMBNAIL] Starting upload for video: $videoId ===")
            
            val userId = auth.currentUser?.uid ?: run {
                Log.e(TAG, "=== [UPLOAD-THUMBNAIL] No authenticated user ===")
                return false
            }
            
            val thumbnailRef = storage.reference
                .child("thumbnails")
                .child(userId)
                .child("thumb_${System.currentTimeMillis()}.jpg")
            
            // Upload thumbnail
            val thumbnailUrl = withTimeout(30000) { // 30 second timeout
                suspendCancellableCoroutine<String?> { continuation ->
                    thumbnailRef.putFile(Uri.fromFile(thumbnailFile))
                        .addOnSuccessListener {
                            thumbnailRef.downloadUrl
                                .addOnSuccessListener { uri ->
                                    continuation.resume(uri.toString()) {
                                        // Handle cancellation by deleting the uploaded file
                                        thumbnailRef.delete()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Failed to get download URL: ${e.message} ===", e)
                                    continuation.resume(null) {
                                        // Handle cancellation by deleting the uploaded file
                                        thumbnailRef.delete()
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Upload failed: ${e.message} ===", e)
                            continuation.resume(null) {
                                // No need to delete anything on failure
                            }
                        }
                        .addOnCanceledListener {
                            continuation.cancel()
                        }
                }
            }
            
            if (thumbnailUrl != null) {
                // Update video document with new thumbnail URL
                withTimeout(10000) { // 10 second timeout
                    firestore.collection("videos")
                        .document(videoId)
                        .update("thumbnailUrl", thumbnailUrl)
                        .await()
                }
                
                Log.d(TAG, "=== [UPLOAD-THUMBNAIL] Successfully updated video with new thumbnail ===")
                true
            } else {
                Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Failed to get thumbnail URL ===")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Error uploading thumbnail: ${e.message} ===", e)
            false
        }
    }
} 