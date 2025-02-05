package com.example.reel_ai.data.repository

import android.util.Log
import com.example.reel_ai.domain.auth.AuthRepository
import com.example.reel_ai.domain.auth.AuthResult
import com.example.reel_ai.domain.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ReelAI_AuthRepo"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        Log.d(TAG, "=== Setting up auth state listener ===")
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val isSignedIn = auth.currentUser != null
            Log.d(TAG, "=== Auth state changed: isSignedIn=$isSignedIn, user=${auth.currentUser?.email} ===")
            trySend(isSignedIn)
        }
        auth.addAuthStateListener(listener)
        awaitClose { 
            Log.d(TAG, "=== Removing auth state listener ===")
            auth.removeAuthStateListener(listener) 
        }
    }

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult<Unit> {
        Log.d(TAG, "=== Attempting to sign in with Google account: ${account.email} ===")
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d(TAG, "=== Created Firebase credential from Google token ===")
            
            auth.signInWithCredential(credential).await()
            Log.d(TAG, "=== Successfully signed in with Firebase ===")
            
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to sign in with Google: ${e.message} ===", e)
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        Log.d(TAG, "=== Attempting to sign out user: ${auth.currentUser?.email} ===")
        return try {
            auth.signOut()
            Log.d(TAG, "=== Successfully signed out ===")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to sign out: ${e.message} ===", e)
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override fun isUserSignedIn(): Boolean = auth.currentUser != null.also {
        Log.d(TAG, "=== Checking if user is signed in: $it, user=${auth.currentUser?.email} ===")
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.d(TAG, "=== No current user ===")
            return null
        }

        Log.d(TAG, "=== Getting current user info: ${firebaseUser.email} ===")
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            handle = firebaseUser.email?.substringBefore("@"),
            bio = null,
            photoUrl = firebaseUser.photoUrl?.toString()
        ).also {
            Log.d(TAG, "=== Created User object with handle: ${it.handle} ===")
        }
    }
} 