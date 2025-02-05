package com.example.reel_ai

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "ReelAI_Application"

@HiltAndroidApp
class ReelAiApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        
        // Set up global error handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "=== Uncaught Exception in thread ${thread.name} ===")
            Log.e(TAG, "=== Exception: ${throwable.message} ===")
            Log.e(TAG, "=== Stack trace: ===")
            Log.e(TAG, throwable.stackTraceToString())
            
            // Rethrow to let the system handle the crash
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of app memory for thumbnails
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of disk space
                    .build()
            }
            .memoryCachePolicy(CachePolicy.DISABLED) // Disable memory cache for thumbnails
            .diskCachePolicy(CachePolicy.DISABLED) // Disable disk cache for thumbnails
            .build()
    }
} 