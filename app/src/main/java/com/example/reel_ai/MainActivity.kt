package com.example.reel_ai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.reel_ai.ui.auth.GoogleAuthUiClient
import com.example.reel_ai.ui.navigation.NavGraph
import com.example.reel_ai.ui.theme.ReelAiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "ReelAI_MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var googleAuthUiClient: GoogleAuthUiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== onCreate called ===")
        
        setContent {
            Log.d(TAG, "=== setContent called ===")
            ReelAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Log.d(TAG, "=== Setting up NavGraph ===")
                    NavGraph(
                        navController = navController,
                        googleAuthUiClient = googleAuthUiClient
                    )
                }
            }
        }
    }
}