package com.example.reel_ai.di

import com.example.reel_ai.data.repository.AuthRepositoryImpl
import com.example.reel_ai.data.repository.VideoRepositoryImpl
import com.example.reel_ai.domain.auth.AuthRepository
import com.example.reel_ai.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ): VideoRepository
} 