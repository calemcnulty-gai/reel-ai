package com.example.reel_ai.data.repository

import android.net.Uri
import android.util.Log
import com.example.reel_ai.domain.model.Video
import com.example.reel_ai.domain.repository.VideoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.FirebaseException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.Continuation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CancellableContinuation
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.reel_ai.domain.video.VideoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flow
import kotlin.math.min
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val TAG = "ReelAI_VideoRepo"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val videoManager: VideoManager,
    @ApplicationContext private val context: Context
) : VideoRepository {

    private val videosCollection = firestore.collection("videos")
    
    // Track upload state
    @Volatile private var isUploadComplete = false
    @Volatile private var uploadSuccess = false
    @Volatile private var uploadCancelled = false
    
    private fun completeUpload(
        continuation: Continuation<Video?>,
        video: Video?,
        isSuccess: Boolean,
        error: Exception? = null
    ) {
        try {
            synchronized(this) {
                if (continuation is CancellableContinuation && continuation.isActive) {
                    isUploadComplete = true
                    uploadSuccess = isSuccess
                    if (isSuccess && video != null) {
                        continuation.resume(video)
                    } else if (error != null) {
                        continuation.resumeWithException(error)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "=== [UPLOAD-COMPLETE] Error during completion: ${e.message} ===", e)
            if (continuation is CancellableContinuation && continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }

    override fun getVideos(limit: Int, startAfter: String?): Flow<List<Video>> = callbackFlow {
        Log.d(TAG, "=== Starting getVideos flow with limit=$limit, startAfter=$startAfter ===")
        var query = videosCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        startAfter?.let { lastVideoId ->
            Log.d(TAG, "=== Fetching startAfter document: $lastVideoId ===")
            val lastVideo = videosCollection.document(lastVideoId).get().await()
            query = query.startAfter(lastVideo)
            Log.d(TAG, "=== Query updated with startAfter document ===")
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "=== Error fetching videos: ${error.message} ===", error)
                return@addSnapshotListener
            }

            Log.d(TAG, "=== Received Firestore snapshot update ===")
            val videos = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Video::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            Log.d(TAG, "=== Mapped ${videos.size} videos, emitting to flow ===")
            trySend(videos)
            Log.d(TAG, "=== Flow emission completed ===")
        }

        awaitClose { 
            Log.d(TAG, "=== Closing getVideos flow, removing snapshot listener ===")
            subscription.remove() 
        }
    }

    override fun getUserVideos(): Flow<List<Video>> = callbackFlow {
        Log.d(TAG, "=== Starting user videos fetch ===")
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "=== Failed to fetch user videos: User not authenticated ===")
            trySend(emptyList())
            return@callbackFlow
        }

        val query = videosCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "=== Error fetching user videos: ${error.message} ===", error)
                return@addSnapshotListener
            }

            val videos = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Video::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            Log.d(TAG, "=== Fetched ${videos.size} videos for user $userId ===")
            trySend(videos)
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun uploadVideo(
        videoFile: File,
        title: String?,
        description: String?,
        onProgress: ((Float) -> Unit)?
    ): Video? = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "=== [UPLOAD-START] Starting upload process ===")
            Log.d(TAG, "=== [UPLOAD-FILE] File: ${videoFile.absolutePath}, size: ${videoFile.length()} bytes ===")
            
            // Reset upload state
            isUploadComplete = false
            uploadSuccess = false
            uploadCancelled = false
            
            val userId = auth.currentUser?.uid ?: run {
                Log.e(TAG, "=== [UPLOAD-AUTH] Upload failed: User not authenticated ===")
                completeUpload(continuation, null, false)
                return@suspendCancellableCoroutine
            }
            Log.d(TAG, "=== [UPLOAD-AUTH] User authenticated: $userId ===")
            
            if (!videoFile.exists()) {
                Log.e(TAG, "=== [UPLOAD-VALIDATE] Upload failed: File does not exist ===")
                completeUpload(continuation, null, false)
                return@suspendCancellableCoroutine
            }
            
            val videoFileName = "video_${System.currentTimeMillis()}.mp4"
            val videoRef = storage.reference
                .child("videos")
                .child(userId)
                .child(videoFileName)
            
            Log.d(TAG, "=== [UPLOAD-STORAGE] Created storage reference: ${videoRef.path} ===")
            
            // Add storage rules sanity check
            Log.d(TAG, "=== [FIRESTORE-SANITY] Storage Rules Check ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] request.auth != null : ${auth.currentUser != null} ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] request.auth.uid == userId : ${auth.currentUser?.uid == userId} ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] request.resource.size < 10MB : ${videoFile.length() < 10 * 1024 * 1024} ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] Actual file size: ${videoFile.length()} bytes ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] Size limit: ${10 * 1024 * 1024} bytes ===")
            Log.d(TAG, "=== [FIRESTORE-SANITY] Storage path: /videos/$userId/$videoFileName ===")
            
            val uploadTask = videoRef.putFile(Uri.fromFile(videoFile))
            
            uploadTask
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d(TAG, "=== [UPLOAD-PROGRESS] Progress: $progress%, ${taskSnapshot.bytesTransferred}/${taskSnapshot.totalByteCount} bytes ===")
                    onProgress?.invoke(progress.toFloat())
                }
                .addOnCanceledListener {
                    // Only handle cancellation if we're not already complete
                    if (!isUploadComplete && !uploadSuccess) {
                        uploadCancelled = true
                        Log.d(TAG, "=== [UPLOAD-CANCEL-TASK] Upload task was cancelled ===")
                        completeUpload(continuation, null, false)
                    }
                }
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "=== [UPLOAD-ERROR] Upload task failed: ${task.exception?.message} ===", task.exception)
                        throw task.exception ?: Exception("Upload failed")
                    }
                    Log.d(TAG, "=== [UPLOAD-URL] Requesting download URL ===")
                    videoRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    Log.d(TAG, "=== [UPLOAD-URL] Got download URL: $downloadUri ===")
                    
                    // Generate and upload thumbnail
                    val thumbnailFile = videoManager.generateThumbnail(videoFile)
                    var thumbnailUrl: String?
                    
                    if (thumbnailFile != null) {
                        Log.d(TAG, "=== [UPLOAD-THUMBNAIL] Uploading thumbnail ===")
                        val thumbnailRef = storage.reference
                            .child("thumbnails")
                            .child(userId)
                            .child("thumb_${System.currentTimeMillis()}.jpg")
                        
                        try {
                            thumbnailRef.putFile(Uri.fromFile(thumbnailFile))
                                .continueWithTask { task ->
                                    if (!task.isSuccessful) {
                                        Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Upload failed: ${task.exception?.message} ===", task.exception)
                                        throw task.exception ?: Exception("Thumbnail upload failed")
                                    }
                                    thumbnailRef.downloadUrl
                                }
                                .addOnSuccessListener { uri ->
                                    thumbnailUrl = uri.toString()
                                    Log.d(TAG, "=== [UPLOAD-THUMBNAIL] Thumbnail uploaded: $thumbnailUrl ===")
                                    
                                    // Create video document
                                    val video = Video(
                                        id = "",
                                        userId = userId,
                                        title = title,
                                        description = description,
                                        videoUrl = downloadUri.toString(),
                                        thumbnailUrl = thumbnailUrl,
                                        createdAt = Date(),
                                        views = 0,
                                        likes = 0,
                                        viewCount = 0,
                                        shareCount = 0
                                    )
                                    
                                    // Add to Firestore
                                    videosCollection.add(video)
                                        .addOnSuccessListener { docRef ->
                                            val finalVideo = video.copy(id = docRef.id)
                                            completeUpload(continuation, finalVideo, true)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "=== [UPLOAD-FIRESTORE] Failed to create document: ${e.message} ===", e)
                                            completeUpload(continuation, null, false)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Failed to get thumbnail URL: ${e.message} ===", e)
                                    // Continue without thumbnail
                                    createVideoDocument(userId, title, description, downloadUri.toString(), null, continuation)
                                }
                        } catch (e: Exception) {
                            Log.e(TAG, "=== [UPLOAD-THUMBNAIL] Error during thumbnail upload: ${e.message} ===", e)
                            // Continue without thumbnail
                            createVideoDocument(userId, title, description, downloadUri.toString(), null, continuation)
                        } finally {
                            thumbnailFile.delete()
                        }
                    } else {
                        // No thumbnail, create video document
                        createVideoDocument(userId, title, description, downloadUri.toString(), null, continuation)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "=== [UPLOAD-ERROR] Failed to get download URL: ${e.message} ===", e)
                    completeUpload(continuation, null, false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "=== [UPLOAD-ERROR] Unexpected error: ${e.message} ===", e)
            completeUpload(continuation, null, false)
        }
    }

    private fun createVideoDocument(
        userId: String,
        title: String?,
        description: String?,
        videoUrl: String,
        thumbnailUrl: String?,
        continuation: CancellableContinuation<Video?>
    ) {
        val video = Video(
            id = "",
            userId = userId,
            title = title,
            description = description,
            videoUrl = videoUrl,
            thumbnailUrl = thumbnailUrl,
            createdAt = Date(),
            views = 0,
            likes = 0,
            viewCount = 0,
            shareCount = 0
        )
        
        videosCollection.add(video)
            .addOnSuccessListener { docRef ->
                val finalVideo = video.copy(id = docRef.id)
                completeUpload(continuation, finalVideo, true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "=== [UPLOAD-FIRESTORE] Failed to create document: ${e.message} ===", e)
                completeUpload(continuation, null, false)
            }
    }

    override suspend fun incrementViewCount(videoId: String) {
        try {
            Log.d(TAG, "=== Incrementing view count for video: $videoId ===")
            videosCollection.document(videoId)
                .update("viewCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Log.d(TAG, "=== Successfully incremented view count ===")
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to increment view count: ${e.message} ===", e)
        }
    }

    override suspend fun toggleLike(videoId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val videoRef = videosCollection.document(videoId)
        val likesCollection = videoRef.collection("likes")
        val userLikeRef = likesCollection.document(userId)

        return try {
            val userLike = userLikeRef.get().await()
            if (userLike.exists()) {
                userLikeRef.delete().await()
                false
            } else {
                userLikeRef.set(mapOf("timestamp" to Date())).await()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle like: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteVideo(videoId: String): Boolean {
        return try {
            Log.d(TAG, "=== Starting video deletion process for: $videoId ===")
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "=== Delete failed: User not authenticated ===")
                return false
            }

            val video = videosCollection.document(videoId).get().await()
                .toObject(Video::class.java)
            if (video == null) {
                Log.e(TAG, "=== Delete failed: Video not found ===")
                return false
            }

            // Check if user owns the video
            if (video.userId != userId) {
                Log.e(TAG, "=== Delete failed: User does not own video ===")
                return false
            }

            // Delete video file from Storage
            val videoUri = Uri.parse(video.videoUrl)
            val videoPath = videoUri.lastPathSegment
            if (videoPath != null) {
                Log.d(TAG, "=== Deleting video file from storage: $videoPath ===")
                storage.reference.child(videoPath).delete().await()
            }

            // Delete video document and its subcollections
            Log.d(TAG, "=== Deleting video document from Firestore ===")
            videosCollection.document(videoId).delete().await()
            Log.d(TAG, "=== Successfully deleted video ===")
            true
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to delete video: ${e.message} ===", e)
            false
        }
    }

    override suspend fun getVideo(videoId: String): Video? {
        return try {
            Log.d(TAG, "=== Getting video: $videoId ===")
            videosCollection.document(videoId).get().await()
                .toObject(Video::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to get video: ${e.message} ===", e)
            null
        }
    }

    override suspend fun incrementShareCount(videoId: String) {
        try {
            Log.d(TAG, "=== Incrementing share count for video: $videoId ===")
            videosCollection.document(videoId)
                .update("shareCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Log.d(TAG, "=== Successfully incremented share count ===")
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to increment share count: ${e.message} ===", e)
        }
    }

    /**
     * Generates and uploads thumbnails for videos that don't have them
     * @return The number of thumbnails successfully generated and uploaded
     */
    override suspend fun generateMissingThumbnails(): Int {
        var successCount = 0
        try {
            Log.d(TAG, "=== [BATCH-THUMBNAILS] Starting missing thumbnails generation ===")
            
            // Get all videos without thumbnails
            val videos = videosCollection
                .whereEqualTo("thumbnailUrl", null)
                .get()
                .await()
                .documents
                .mapNotNull { doc -> 
                    doc.toObject(Video::class.java)?.copy(id = doc.id)
                }
            
            Log.d(TAG, "=== [BATCH-THUMBNAILS] Found ${videos.size} videos without thumbnails ===")
            Log.d(TAG, "=== [BATCH-THUMBNAILS] Starting main processing loop ===")
            
            // Process videos in parallel with a maximum of 3 concurrent operations
            withContext(Dispatchers.IO) {
                val results = mutableListOf<Boolean>()
                val batchSize = 3
                
                for (i in videos.indices step batchSize) {
                    val endIndex = min(i + batchSize, videos.size)
                    val batch = videos.subList(i, endIndex)
                    
                    val batchResults = batch.mapIndexed { batchIndex, currentVideo ->
                        async {
                            var success = false
                            val index = i + batchIndex
                            try {
                                Log.d(TAG, "=== [BATCH-THUMBNAILS] LOOP START: Processing video ${index + 1}/${videos.size} ===")
                                Log.d(TAG, "=== [BATCH-THUMBNAILS] Video ID: ${currentVideo.id} ===")
                                Log.d(TAG, "=== [BATCH-THUMBNAILS] Video URL: ${currentVideo.videoUrl} ===")
                                Log.d(TAG, "=== [BATCH-THUMBNAILS] User ID: ${currentVideo.userId} ===")
                                
                                // Check if thumbnail already exists
                                val existingVideo = videosCollection.document(currentVideo.id).get().await()
                                    .toObject(Video::class.java)
                                if (existingVideo?.thumbnailUrl != null) {
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Thumbnail already exists for video ${currentVideo.id}, skipping ===")
                                    return@async true
                                }
                                
                                // Download the video file
                                val videoFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_${index}.mp4")
                                try {
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Downloading video to: ${videoFile.absolutePath} ===")
                                    
                                    val videoRef = storage.getReferenceFromUrl(currentVideo.videoUrl)
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Video storage path: ${videoRef.path} ===")
                                    
                                    withTimeout(60000) { // 60 second timeout for download
                                        videoRef.getFile(videoFile).await()
                                    }
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Video downloaded successfully, size: ${videoFile.length()} bytes ===")
                                    
                                    // Generate thumbnail
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Generating thumbnail ===")
                                    val thumbnailFile = videoManager.generateThumbnail(videoFile)
                                    
                                    if (thumbnailFile != null) {
                                        try {
                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Thumbnail generated: ${thumbnailFile.absolutePath} ===")
                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Thumbnail size: ${thumbnailFile.length()} bytes ===")
                                            
                                            // Upload thumbnail
                                            val thumbnailRef = storage.reference
                                                .child("thumbnails")
                                                .child(currentVideo.userId)
                                                .child("thumb_${System.currentTimeMillis()}_${index}.jpg")
                                            
                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Thumbnail storage path: ${thumbnailRef.path} ===")
                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Current auth state: ${auth.currentUser != null} ===")
                                            if (auth.currentUser != null) {
                                                Log.d(TAG, "=== [BATCH-THUMBNAILS] Auth UID matches video userId: ${auth.currentUser?.uid == currentVideo.userId} ===")
                                            }
                                            
                                            val thumbnailUrl = withTimeout(30000) { // 30 second timeout for upload
                                                suspendCancellableCoroutine<String?> { continuation ->
                                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Starting thumbnail upload ===")
                                                    
                                                    thumbnailRef.putFile(Uri.fromFile(thumbnailFile))
                                                        .addOnProgressListener { taskSnapshot ->
                                                            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Upload progress for video ${index + 1}: $progress% (${taskSnapshot.bytesTransferred}/${taskSnapshot.totalByteCount} bytes) ===")
                                                        }
                                                        .addOnSuccessListener {
                                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Thumbnail upload successful for video ${index + 1}, getting download URL ===")
                                                            thumbnailRef.downloadUrl
                                                                .addOnSuccessListener { uri ->
                                                                    val url = uri.toString()
                                                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Got thumbnail URL for video ${index + 1}: $url ===")
                                                                    continuation.resume(url)
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.e(TAG, "=== [BATCH-THUMBNAILS] Failed to get download URL for video ${index + 1}: ${e.message} ===", e)
                                                                    continuation.resume(null)
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e(TAG, "=== [BATCH-THUMBNAILS] Failed to upload thumbnail for video ${index + 1}: ${e.message} ===", e)
                                                            continuation.resume(null)
                                                        }
                                                }
                                            }
                                            
                                            if (thumbnailUrl != null) {
                                                Log.d(TAG, "=== [BATCH-THUMBNAILS] Updating Firestore document with thumbnail URL for video ${index + 1} ===")
                                                withTimeout(10000) { // 10 second timeout for Firestore update
                                                    videosCollection.document(currentVideo.id)
                                                        .update("thumbnailUrl", thumbnailUrl)
                                                        .await()
                                                }
                                                
                                                success = true
                                                Log.d(TAG, "=== [BATCH-THUMBNAILS] Successfully updated video ${currentVideo.id} with thumbnail ===")
                                            } else {
                                                Log.e(TAG, "=== [BATCH-THUMBNAILS] Failed to get thumbnail URL for video ${currentVideo.id} ===")
                                            }
                                        } finally {
                                            Log.d(TAG, "=== [BATCH-THUMBNAILS] Cleaning up thumbnail file for video ${index + 1} ===")
                                            thumbnailFile.delete()
                                        }
                                    } else {
                                        Log.e(TAG, "=== [BATCH-THUMBNAILS] Failed to generate thumbnail for video ${currentVideo.id} ===")
                                    }
                                } finally {
                                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Cleaning up video file for video ${index + 1} ===")
                                    videoFile.delete()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "=== [BATCH-THUMBNAILS] Error processing video ${currentVideo.id}: ${e.message} ===", e)
                            } finally {
                                Log.d(TAG, "=== [BATCH-THUMBNAILS] LOOP END: Completed processing video ${index + 1}/${videos.size} ===")
                            }
                            success
                        }
                    }
                    
                    results.addAll(batchResults.awaitAll())
                    Log.d(TAG, "=== [BATCH-THUMBNAILS] Completed batch ${i / batchSize + 1}, total success: ${results.count { it }}/${results.size} ===")
                }
                
                successCount = results.count { it }
            }
            
            Log.d(TAG, "=== [BATCH-THUMBNAILS] Main processing loop completed ===")
            Log.d(TAG, "=== [BATCH-THUMBNAILS] Final results: Success: $successCount/${videos.size} ===")
        } catch (e: Exception) {
            Log.e(TAG, "=== [BATCH-THUMBNAILS] Fatal error in thumbnail generation: ${e.message} ===", e)
            Log.e(TAG, "=== [BATCH-THUMBNAILS] Error type: ${e.javaClass.simpleName} ===")
        }
        return successCount
    }

    override suspend fun getVideosNeedingThumbnails(): List<Video> {
        return try {
            Log.d(TAG, "=== [GET-VIDEOS] Getting videos without thumbnails ===")
            videosCollection
                .whereEqualTo("thumbnailUrl", null)
                .get()
                .await()
                .documents
                .mapNotNull { doc -> 
                    doc.toObject(Video::class.java)?.copy(id = doc.id)
                }
                .also { videos ->
                    Log.d(TAG, "=== [GET-VIDEOS] Found ${videos.size} videos without thumbnails ===")
                }
        } catch (e: Exception) {
            Log.e(TAG, "=== [GET-VIDEOS] Error getting videos: ${e.message} ===", e)
            emptyList()
        }
    }

    override suspend fun generateThumbnailForVideo(video: Video): Boolean {
        return try {
            Log.d(TAG, "=== [GENERATE-THUMBNAIL] Starting for video ${video.id} ===")
            
            // Check if thumbnail already exists
            val existingVideo = videosCollection.document(video.id).get().await()
                .toObject(Video::class.java)
            if (existingVideo?.thumbnailUrl != null) {
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Thumbnail already exists, skipping ===")
                return true
            }
            
            // Download the video file
            val videoFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.mp4")
            try {
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Downloading video to: ${videoFile.absolutePath} ===")
                
                val videoRef = storage.getReferenceFromUrl(video.videoUrl)
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Video storage path: ${videoRef.path} ===")
                
                withTimeout(60000) { // 60 second timeout for download
                    videoRef.getFile(videoFile).await()
                }
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Video downloaded successfully, size: ${videoFile.length()} bytes ===")
                
                // Generate thumbnail
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Generating thumbnail ===")
                val thumbnailFile = videoManager.generateThumbnail(videoFile)
                
                if (thumbnailFile != null) {
                    try {
                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Thumbnail generated: ${thumbnailFile.absolutePath} ===")
                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Thumbnail size: ${thumbnailFile.length()} bytes ===")
                        
                        // Upload thumbnail
                        val thumbnailRef = storage.reference
                            .child("thumbnails")
                            .child(video.userId)
                            .child("thumb_${System.currentTimeMillis()}.jpg")
                        
                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Thumbnail storage path: ${thumbnailRef.path} ===")
                        
                        val thumbnailUrl = withTimeout(30000) { // 30 second timeout for upload
                            suspendCancellableCoroutine<String?> { continuation ->
                                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Starting thumbnail upload ===")
                                
                                thumbnailRef.putFile(Uri.fromFile(thumbnailFile))
                                    .addOnProgressListener { taskSnapshot ->
                                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Upload progress: $progress% (${taskSnapshot.bytesTransferred}/${taskSnapshot.totalByteCount} bytes) ===")
                                    }
                                    .addOnSuccessListener {
                                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Thumbnail upload successful, getting download URL ===")
                                        thumbnailRef.downloadUrl
                                            .addOnSuccessListener { uri ->
                                                val url = uri.toString()
                                                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Got thumbnail URL: $url ===")
                                                continuation.resume(url)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "=== [GENERATE-THUMBNAIL] Failed to get download URL: ${e.message} ===", e)
                                                continuation.resume(null)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "=== [GENERATE-THUMBNAIL] Failed to upload thumbnail: ${e.message} ===", e)
                                        continuation.resume(null)
                                    }
                            }
                        }
                        
                        if (thumbnailUrl != null) {
                            Log.d(TAG, "=== [GENERATE-THUMBNAIL] Updating Firestore document with thumbnail URL ===")
                            withTimeout(10000) { // 10 second timeout for Firestore update
                                videosCollection.document(video.id)
                                    .update("thumbnailUrl", thumbnailUrl)
                                    .await()
                            }
                            
                            Log.d(TAG, "=== [GENERATE-THUMBNAIL] Successfully updated video with thumbnail ===")
                            return true
                        } else {
                            Log.e(TAG, "=== [GENERATE-THUMBNAIL] Failed to get thumbnail URL ===")
                        }
                    } finally {
                        Log.d(TAG, "=== [GENERATE-THUMBNAIL] Cleaning up thumbnail file ===")
                        thumbnailFile.delete()
                    }
                } else {
                    Log.e(TAG, "=== [GENERATE-THUMBNAIL] Failed to generate thumbnail ===")
                }
            } finally {
                Log.d(TAG, "=== [GENERATE-THUMBNAIL] Cleaning up video file ===")
                videoFile.delete()
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "=== [GENERATE-THUMBNAIL] Error generating thumbnail: ${e.message} ===", e)
            false
        }
    }

    override suspend fun updateVideo(
        videoId: String,
        title: String?,
        description: String?,
        thumbnailUrl: String?
    ): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "=== [UPDATE] Updating video $videoId ===")
            Log.d(TAG, "=== [UPDATE] Title: $title ===")
            Log.d(TAG, "=== [UPDATE] Description: $description ===")
            Log.d(TAG, "=== [UPDATE] Thumbnail URL: $thumbnailUrl ===")

            val updates = mutableMapOf<String, Any?>()
            title?.let { updates["title"] = it }
            description?.let { updates["description"] = it }
            thumbnailUrl?.let { updates["thumbnailUrl"] = it }

            if (updates.isEmpty()) {
                Log.d(TAG, "=== [UPDATE] No updates provided ===")
                continuation.resume(true) { }
                return@suspendCancellableCoroutine
            }

            videosCollection.document(videoId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "=== [UPDATE] Successfully updated video $videoId ===")
                    continuation.resume(true) { }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "=== [UPDATE] Failed to update video: ${e.message} ===", e)
                    continuation.resume(false) { }
                }
        } catch (e: Exception) {
            Log.e(TAG, "=== [UPDATE] Unexpected error: ${e.message} ===", e)
            continuation.resume(false) { }
        }
    }
} 