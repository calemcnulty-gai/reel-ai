package com.example.reel_ai.domain.auth

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow
import com.example.reel_ai.domain.model.User

interface AuthRepository {
    fun getAuthState(): Flow<Boolean>
    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult<Unit>
    suspend fun signOut(): AuthResult<Unit>
    fun isUserSignedIn(): Boolean
    fun getCurrentUser(): User?
} 