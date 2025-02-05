package com.example.reel_ai.domain.repository

import android.net.Uri
import com.example.reel_ai.domain.model.Video
import kotlinx.coroutines.flow.Flow
import java.io.File

interface VideoRepository {
    /**
     * Get a flow of videos for the feed, ordered by creation date
     * @param limit Number of videos to load at a time
     * @param startAfter Video ID to start after for pagination
     * @return Flow of list of videos
     */
    fun getVideos(limit: Int = 10, startAfter: String? = null): Flow<List<Video>>
    
    /**
     * Get a flow of videos for the current user
     * @return Flow of list of videos
     */
    fun getUserVideos(): Flow<List<Video>>
    
    /**
     * Upload a video file to Firebase Storage and create its metadata in Firestore
     * @param videoFile The local video file to upload
     * @param title Optional title for the video
     * @param description Optional description for the video
     * @param onProgress Optional callback for upload progress (0-100)
     * @return The created Video object if successful, null if failed
     */
    suspend fun uploadVideo(
        videoFile: File,
        title: String? = null,
        description: String? = null,
        onProgress: ((Float) -> Unit)? = null
    ): Video?
    
    /**
     * Get a video by its ID
     * @param videoId ID of the video to get
     * @return The video if found, null otherwise
     */
    suspend fun getVideo(videoId: String): Video?
    
    /**
     * Increment the view count for a video
     * @param videoId ID of the video to increment views for
     */
    suspend fun incrementViewCount(videoId: String)
    
    /**
     * Increment the share count for a video
     * @param videoId ID of the video to increment shares for
     */
    suspend fun incrementShareCount(videoId: String)
    
    /**
     * Delete a video and its associated files
     * @param videoId ID of the video to delete
     * @return true if successful, false otherwise
     */
    suspend fun deleteVideo(videoId: String): Boolean
    
    /**
     * Toggle like status for a video
     * @param videoId ID of the video to like/unlike
     * @return true if video is now liked, false if unliked
     */
    suspend fun toggleLike(videoId: String): Boolean
    
    /**
     * Generates thumbnails for videos that don't have them
     * @return The number of thumbnails successfully generated
     */
    suspend fun generateMissingThumbnails(): Int

    /**
     * Gets a list of videos that need thumbnails generated
     * @return List of videos without thumbnails
     */
    suspend fun getVideosNeedingThumbnails(): List<Video>

    /**
     * Generates and uploads a thumbnail for a single video
     * @param video The video to generate a thumbnail for
     * @return true if successful, false otherwise
     */
    suspend fun generateThumbnailForVideo(video: Video): Boolean

    /**
     * Update a video's metadata
     * @param videoId ID of the video to update
     * @param title Optional new title for the video
     * @param description Optional new description for the video
     * @param thumbnailUrl Optional new thumbnail URL for the video
     * @return true if successful, false otherwise
     */
    suspend fun updateVideo(
        videoId: String,
        title: String? = null,
        description: String? = null,
        thumbnailUrl: String? = null
    ): Boolean
} 