package com.example.reel_ai.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reel_ai.domain.auth.AuthRepository
import com.example.reel_ai.domain.auth.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReelAI_AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "=== Initializing AuthViewModel ===")
        viewModelScope.launch {
            repository.getAuthState().collect { isSignedIn ->
                Log.d(TAG, "=== Auth state update: isSignedIn=$isSignedIn ===")
                _uiState.update { it.copy(isSignedIn = isSignedIn) }
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "=== Attempting to sign in with Google account: ${account.email} ===")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.signInWithGoogle(account)) {
                is AuthResult.Success -> {
                    Log.d(TAG, "=== Sign in successful ===")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = null
                    )}
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "=== Sign in failed: ${result.message} ===")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
                is AuthResult.Loading -> {
                    Log.d(TAG, "=== Sign in loading ===")
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signOut() {
        Log.d(TAG, "=== Attempting to sign out ===")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.signOut()) {
                is AuthResult.Success -> {
                    Log.d(TAG, "=== Sign out successful ===")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = null
                    )}
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "=== Sign out failed: ${result.message} ===")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
                is AuthResult.Loading -> {
                    Log.d(TAG, "=== Sign out loading ===")
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun updateError(message: String?) {
        Log.d(TAG, "=== Updating error: $message ===")
        _uiState.update { it.copy(error = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 