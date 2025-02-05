package com.example.reel_ai.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.example.reel_ai.ui.common.ErrorScreen

private const val TAG = "ReelAI_LoginScreen"

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    googleAuthUiClient: GoogleAuthUiClient
) {
    Log.d(TAG, "=== LoginScreen composing ===")
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            Log.d(TAG, "=== Google sign in result received: resultCode=${result.resultCode} ===")
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "=== Sign in result OK, getting account ===")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    Log.d(TAG, "=== Sign in task created, checking if complete ===")
                    try {
                        if (!task.isComplete) {
                            Log.d(TAG, "=== Sign in task not complete ===")
                            return@rememberLauncherForActivityResult
                        }
                        if (task.isCanceled) {
                            Log.d(TAG, "=== Sign in task was canceled ===")
                            return@rememberLauncherForActivityResult
                        }
                        Log.d(TAG, "=== Getting account from completed task ===")
                        val account = task.getResult(ApiException::class.java)
                        Log.d(TAG, "=== Successfully got Google account: ${account.email} ===")
                        viewModel.signInWithGoogle(account)
                    } catch (e: ApiException) {
                        Log.e(TAG, "=== Google sign in failed with status: ${e.statusCode} ===", e)
                        when (e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d(TAG, "=== User cancelled sign in ===")
                                viewModel.updateError("Sign in cancelled")
                            }
                            CommonStatusCodes.INTERNAL_ERROR -> {
                                Log.e(TAG, "=== Internal error during sign in ===")
                                viewModel.updateError("Internal error during sign in")
                            }
                            CommonStatusCodes.NETWORK_ERROR -> {
                                Log.e(TAG, "=== Network error during sign in ===")
                                viewModel.updateError("Network error during sign in")
                            }
                            CommonStatusCodes.INVALID_ACCOUNT -> {
                                Log.e(TAG, "=== Invalid account error during sign in ===")
                                viewModel.updateError("Invalid account")
                            }
                            CommonStatusCodes.SIGN_IN_REQUIRED -> {
                                Log.d(TAG, "=== Sign in required ===")
                                viewModel.updateError("Sign in required")
                            }
                            else -> {
                                Log.e(TAG, "=== Sign in failed with code ${e.statusCode}: ${e.message} ===")
                                viewModel.updateError("Google sign in failed: ${e.message}")
                            }
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "=== User cancelled sign in ===")
                    viewModel.updateError("Sign in cancelled")
                }
                else -> {
                    Log.e(TAG, "=== Sign in failed with result code: ${result.resultCode} ===")
                    viewModel.updateError("Sign in failed")
                }
            }
        }
    )

    if (uiState.isSignedIn) {
        Log.d(TAG, "=== User is signed in, navigating to success ===")
        onSignInSuccess()
        return
    }

    if (uiState.error != null) {
        ErrorScreen(
            message = uiState.error!!,
            onDismiss = { viewModel.clearError() },
            action = {
                viewModel.clearError()
                try {
                    launcher.launch(googleAuthUiClient.getSignInIntent())
                } catch (e: Exception) {
                    viewModel.updateError("Failed to start sign in: ${e.message}")
                }
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to ReelAI",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sign in to start creating",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { 
                    Log.d(TAG, "=== Sign in button clicked ===")
                    try {
                        launcher.launch(googleAuthUiClient.getSignInIntent())
                        Log.d(TAG, "=== Sign in intent launched successfully ===")
                    } catch (e: Exception) {
                        Log.e(TAG, "=== Failed to launch sign in intent: ${e.message} ===", e)
                        viewModel.updateError("Failed to start sign in: ${e.message}")
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sign in with Google")
            }

            if (uiState.isLoading) {
                Log.d(TAG, "=== Showing loading indicator ===")
                CircularProgressIndicator()
            }
        }
    }
} 