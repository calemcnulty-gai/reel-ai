package com.example.reel_ai.ui.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import javax.inject.Inject

private const val TAG = "ReelAI_GoogleAuth"

class GoogleAuthUiClient @Inject constructor(
    private val context: Context
) {
    private val signInClient: GoogleSignInClient by lazy {
        Log.d(TAG, "=== Initializing GoogleSignInClient ===")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(WEB_CLIENT_ID)
            .build()
        Log.d(TAG, "=== GSO configured with web client ID: ${WEB_CLIENT_ID.take(15)}... ===")
        
        GoogleSignIn.getClient(context, gso).also {
            Log.d(TAG, "=== GoogleSignInClient created successfully ===")
        }
    }

    fun getSignInIntent(): Intent {
        Log.d(TAG, "=== Getting sign in intent ===")
        return try {
            signInClient.signInIntent.also {
                Log.d(TAG, "=== Sign in intent created successfully ===")
            }
        } catch (e: Exception) {
            Log.e(TAG, "=== Failed to create sign in intent: ${e.message} ===", e)
            throw e
        }
    }

    fun getLastSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context).also {
        Log.d(TAG, "=== Getting last signed in account: ${it?.email ?: "null"} ===")
    }

    companion object {
        private const val WEB_CLIENT_ID = "479084412820-jqufhm38anqk6jdsrhcitmet8nbmi25g.apps.googleusercontent.com"
    }
} 