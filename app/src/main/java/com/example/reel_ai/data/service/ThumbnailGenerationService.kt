package com.example.reel_ai.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.reel_ai.domain.repository.VideoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.PendingIntent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import android.app.ForegroundServiceStartNotAllowedException
import android.app.ServiceStartNotAllowedException
import android.content.pm.ServiceInfo
import android.util.Log

@AndroidEntryPoint
class ThumbnailGenerationService : Service() {
    @Inject
    lateinit var videoRepository: VideoRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    
    companion object {
        private const val TAG = "ReelAI_ThumbnailService"
        private const val CHANNEL_ID = "thumbnail_generation_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.reel_ai.START_THUMBNAIL_GENERATION"
        const val ACTION_STOP = "com.example.reel_ai.STOP_THUMBNAIL_GENERATION"
        const val PROGRESS_UPDATE = "com.example.reel_ai.THUMBNAIL_PROGRESS"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_TOTAL = "total"
        const val EXTRA_SUCCESS = "success"
        private const val BATCH_SIZE = 3 // Process 3 videos concurrently
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startGeneration()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startGeneration() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Generating Thumbnails")
            .setContentText("Starting...")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setOngoing(true)
            .setProgress(0, 0, true)
            
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
            } else {
                startForeground(NOTIFICATION_ID, notification.build())
            }
            
            serviceScope.launch {
                try {
                    var successCount = 0
                    var totalProcessed = 0
                    
                    // Get list of videos needing thumbnails
                    val videos = videoRepository.getVideosNeedingThumbnails()
                    val total = videos.size
                    
                    // Process videos in parallel batches
                    videos.asFlow()
                        .buffer(BATCH_SIZE)
                        .map { video ->
                            supervisorScope {
                                try {
                                    val result = videoRepository.generateThumbnailForVideo(video)
                                    if (result) successCount++ 
                                    totalProcessed++
                                    updateProgress(totalProcessed, total, successCount)
                                    result
                                } catch (e: Exception) {
                                    totalProcessed++
                                    updateProgress(totalProcessed, total, successCount, "Failed: ${e.message}")
                                    false
                                }
                            }
                        }
                        .collect()
                    
                    // Final update and cleanup
                    updateProgress(total, total, successCount)
                    stopSelf()
                } catch (e: Exception) {
                    updateProgress(0, 0, 0, "Service error: ${e.message}")
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            when (e) {
                is ForegroundServiceStartNotAllowedException,
                is ServiceStartNotAllowedException -> {
                    Log.e(TAG, "=== Failed to start service: ${e.message} ===")
                    stopSelf()
                }
                else -> throw e
            }
        }
    }

    private fun updateProgress(current: Int, total: Int, success: Int, error: String? = null) {
        // Update notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Generating Thumbnails")
            .setContentText(error ?: "$success/$total thumbnails generated")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setOngoing(true)
            .setProgress(total, current, false)

        notificationManager.notify(NOTIFICATION_ID, notification.build())

        // Broadcast progress
        val intent = Intent(PROGRESS_UPDATE).apply {
            putExtra(EXTRA_PROGRESS, current)
            putExtra(EXTRA_TOTAL, total)
            putExtra(EXTRA_SUCCESS, success)
            error?.let { putExtra("error", it) }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Thumbnail Generation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows thumbnail generation progress"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
} 