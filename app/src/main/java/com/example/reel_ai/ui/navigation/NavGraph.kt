package com.example.reel_ai.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri
import java.io.File
import com.example.reel_ai.ui.auth.LoginScreen
import com.example.reel_ai.ui.auth.GoogleAuthUiClient
import com.example.reel_ai.ui.auth.AuthViewModel
import com.example.reel_ai.ui.camera.CameraScreen
import com.example.reel_ai.ui.feed.FeedScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reel_ai.ui.profile.ProfileScreen
import com.example.reel_ai.ui.preview.PreviewScreen
import com.example.reel_ai.ui.upload.UploadScreen
import com.example.reel_ai.ui.edit.EditVideoScreen
import com.example.reel_ai.ui.discard.DiscardVideoScreen
import com.example.reel_ai.ui.common.ErrorScreen
import com.example.reel_ai.ui.video.VideoScreen
import java.net.URLDecoder
import java.net.URLEncoder
import com.example.reel_ai.ui.videoedit.VideoEditScreen

private const val TAG = "ReelAI_NavGraph"

const val FEED_TARGET_VIDEO_ARG = "targetVideoId"

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Feed : Screen("feed")
    data object Camera : Screen("camera")
    data object Profile : Screen("profile")
    data object Video : Screen("video/{videoId}") {
        fun createRoute(videoId: String): String {
            val encodedId = URLEncoder.encode(videoId, "UTF-8")
            return "video/$encodedId"
        }
    }
    data object Preview : Screen("preview/{filePath}") {
        fun createRoute(filePath: String): String {
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")
            return "preview/$encodedPath"
        }
    }
    data object Upload : Screen("upload/{filePath}") {
        fun createRoute(filePath: String): String {
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")
            return "upload/$encodedPath"
        }
    }
    data object Edit : Screen("edit/{filePath}") {
        fun createRoute(filePath: String): String {
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")
            return "edit/$encodedPath"
        }
    }
    data object Discard : Screen("discard/{filePath}") {
        fun createRoute(filePath: String): String {
            val encodedPath = URLEncoder.encode(filePath, "UTF-8")
            return "discard/$encodedPath"
        }
    }
    data object Error : Screen("error/{message}") {
        fun createRoute(message: String): String {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            return "error/$encodedMessage"
        }
    }
    data object VideoEdit : Screen("video_edit/{videoId}") {
        fun createRoute(videoId: String) = "video_edit/$videoId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val startDestination = if (authState.isSignedIn) Screen.Feed.route else Screen.Auth.route
    
    Log.d(TAG, "=== Setting up NavHost with start destination: $startDestination ===")
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                googleAuthUiClient = googleAuthUiClient
            )
        }

        composable(
            route = "feed?$FEED_TARGET_VIDEO_ARG={$FEED_TARGET_VIDEO_ARG}",
            arguments = listOf(
                navArgument(FEED_TARGET_VIDEO_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val targetVideoId = entry.arguments?.getString(FEED_TARGET_VIDEO_ARG)
            FeedScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToProfile = { navController.navigate("profile") },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                targetVideoId = targetVideoId
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateToPreview = { file ->
                    navController.navigate(Screen.Preview.createRoute(file.absolutePath))
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVideo = { videoId ->
                    navController.navigate(Screen.Video.createRoute(videoId))
                },
                onNavigateToEditVideo = { videoId ->
                    navController.navigate(Screen.VideoEdit.createRoute(videoId))
                }
            )
        }

        composable(
            route = Screen.Preview.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            val file = File(filePath)
            
            if (!file.exists()) {
                Log.e(TAG, "=== Preview file does not exist: $filePath ===")
                navController.navigate(Screen.Error.createRoute("Video file not found"))
                return@composable
            }
            
            PreviewScreen(
                file = file,
                onNavigateToUpload = { videoFile ->
                    navController.navigate(Screen.Upload.createRoute(videoFile.absolutePath))
                },
                onNavigateToEdit = { videoFile ->
                    navController.navigate(Screen.Edit.createRoute(videoFile.absolutePath))
                },
                onNavigateToDiscard = { videoFile ->
                    navController.navigate(Screen.Discard.createRoute(videoFile.absolutePath))
                },
                onShowError = { message ->
                    navController.navigate(Screen.Error.createRoute(message))
                }
            )
        }

        composable(
            route = Screen.Upload.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            val file = File(filePath)
            
            if (!file.exists()) {
                Log.e(TAG, "=== Upload file does not exist: $filePath ===")
                navController.navigate(Screen.Error.createRoute("Video file not found"))
                return@composable
            }
            
            UploadScreen(
                file = file,
                initialTitle = navController.previousBackStackEntry?.savedStateHandle?.get<String>("title"),
                initialDescription = navController.previousBackStackEntry?.savedStateHandle?.get<String>("description"),
                onUploadComplete = { videoId ->
                    navController.navigate("feed?targetVideoId=$videoId") {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
                onError = { message ->
                    navController.navigate(Screen.Error.createRoute(message))
                }
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            val file = File(filePath)
            
            if (!file.exists()) {
                Log.e(TAG, "=== Edit file does not exist: $filePath ===")
                navController.navigate(Screen.Error.createRoute("Video file not found"))
                return@composable
            }
            
            EditVideoScreen(
                file = file,
                onNavigateToUpload = { videoFile, title, description ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("title", title)
                    navController.currentBackStackEntry?.savedStateHandle?.set("description", description)
                    navController.navigate(Screen.Upload.createRoute(videoFile.absolutePath)) {
                        popUpTo(Screen.Edit.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Discard.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            val file = File(filePath)
            
            if (!file.exists()) {
                Log.e(TAG, "=== Discard file does not exist: $filePath ===")
                navController.navigate(Screen.Error.createRoute("Video file not found"))
                return@composable
            }
            
            DiscardVideoScreen(
                file = file,
                onDiscardConfirm = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Video.route,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            VideoScreen(
                videoId = videoId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onError = { message ->
                    navController.navigate(Screen.Error.createRoute(message))
                }
            )
        }

        composable(
            route = Screen.Error.route,
            arguments = listOf(
                navArgument("message") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val message = URLDecoder.decode(
                backStackEntry.arguments?.getString("message") ?: "",
                "UTF-8"
            )
            
            ErrorScreen(
                message = message,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.VideoEdit.route,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            VideoEditScreen(
                videoId = videoId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 