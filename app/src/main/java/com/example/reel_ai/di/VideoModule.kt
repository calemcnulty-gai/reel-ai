package com.example.reel_ai.di

import android.content.Context
import com.example.reel_ai.domain.video.VideoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoModule {
    @Provides
    @Singleton
    fun provideVideoManager(
        @ApplicationContext context: Context,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): VideoManager = VideoManager(context, storage, auth, firestore)
} 