package com.example.reel_ai.domain.model

data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val handle: String?,
    val bio: String?,
    val photoUrl: String?
) 