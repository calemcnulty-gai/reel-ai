package com.example.reel_ai.domain.model

import java.util.Date

/**
 * Represents a video in the feed
 * @property id Unique identifier for the video
 * @property userId ID of the user who uploaded the video
 * @property title Optional title of the video
 * @property description Optional description of the video
 * @property videoUrl URL to the video file in Firebase Storage
 * @property thumbnailUrl Optional URL to the video thumbnail
 * @property createdAt When the video was uploaded
 * @property views Number of times the video has been viewed
 * @property likes Number of likes the video has received
 * @property viewCount Number of times the video has been viewed
 * @property shareCount Number of times the video has been shared
 */
data class Video(
    val id: String = "",
    val userId: String = "",
    val title: String? = null,
    val description: String? = null,
    val videoUrl: String = "",
    val thumbnailUrl: String? = null,
    val createdAt: Date = Date(),
    val views: Long = 0,
    val likes: Long = 0,
    val viewCount: Int = 0,
    val shareCount: Int = 0
) 