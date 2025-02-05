# ReelAI Project Entry Point

## ⚠️ IMPORTANT: Project Directory Structure
All work MUST be done in the `reel-ai` directory. The complete project structure is:
```
reel-ai/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/reel_ai/  <- All Kotlin files go here
│           ├── res/                       <- All resources go here
│           └── AndroidManifest.xml
├── build.gradle.kts
└── app/build.gradle.kts
```

## Core Documentation
- [Project Checklist](project-checklist.md) - Current progress and upcoming tasks
- [Agent Instructions](agent.md) - AI assistant behavior and requirements
- [Project Overview](ProjectOverview.md) - High-level project description
- [Workflow](workflow.md) - Development workflow and processes
- [Dead Ends](deadends.md) - Failed implementation attempts and lessons learned

## Development Environment Setup
1. Required Tools
   - Android Studio (Latest stable version)
   - JDK 17 or higher
   - Git
   - Firebase CLI

2. Initial Setup
   - Clone the repository
   - Open project in Android Studio
   - Sync project with Gradle files
   - Add google-services.json to app/
   - Configure Firebase project

3. Environment Variables
   - No environment variables required for local development
   - Firebase configuration is handled via google-services.json

4. Running the Project
   - Use Android Studio's built-in emulator
   - Or connect a physical Android device
   - Minimum SDK: Android 26 (Oreo)
   - Target SDK: Android 34 (API 34)

## Android Studio Basics
Common IDE tasks and how to perform them:

### After Code Changes
1. Sync Project with Gradle Files
   - Look for elephant icon with blue arrow (top-right toolbar)
   - Click to sync after changes to build files or dependencies
   - Wait for sync to complete (progress shown at bottom)

2. Build & Run
   - Green "Play" button in top toolbar
   - Select your emulator/device from dropdown
   - Wait for build and deployment

### When Errors Occur
Most useful information to share (in order of importance):
1. Build/Compilation Errors
   - Look in "Build" output tab at bottom
   - Copy the full error message and stack trace
   - Most important are lines starting with "e:" for Kotlin errors

2. Runtime Errors
   - Look in "Logcat" tab at bottom
   - Filter by "Error" level
   - Copy the relevant error and stack trace

3. Resource Errors
   - Look in "Build" output for "Android resource linking failed"
   - Include the full error message showing which resources failed

4. Sync Errors
   - Look in "Sync" tab or "Event Log" at bottom
   - Copy any red error messages

### Troubleshooting
- If seeing unexpected errors:
  1. Try Sync Project with Gradle Files first
  2. If errors persist: File -> Invalidate Caches -> Invalidate and Restart
  3. If still having issues, check Event Log (bottom toolbar) for details

### Navigation
- Project view: Left sidebar, folder icon
- Build output: Bottom panel, "Build" tab
- Run output: Bottom panel, "Run" tab
- Problems view: Bottom panel, "Problems" tab
- Logcat: Bottom panel, "Logcat" tab (for runtime logs)

## Project Status
Current Phase: Phase 1 (PoC)
Progress:
- PoC: 45% complete
- MVP: 0% complete
- Polish: 10% complete

Next Deadline: MVP Deadline (Feb 5, 2025, 6:00 PM CST)

## Build Status
Last Successful Build: Pending
Current Issues:
- None reported

## Current Focus
1. Video Upload (PoC - In Progress)
   - Create upload confirmation screen
   - Implement basic upload functionality
   - Add upload progress indicator
   - Set up video metadata storage in Firestore

2. Video Playback (PoC - Next Up)
   - Create vertical swipe feed
   - Implement basic video loading
   - Add play/pause functionality
   - Implement video caching

3. Video Enhancement (MVP - Planning)
   - Add caption/description support
   - Implement proper video metadata
   - Add basic engagement metrics

## Architecture Overview
- MVVM Architecture with Clean Architecture principles
- Kotlin with Jetpack Compose for UI
- Firebase for backend services
- Hilt for dependency injection

## Firebase Upload Pattern
When implementing Firebase uploads with coroutines, follow this pattern to ensure reliable completion:

1. Use class-level state tracking:
```kotlin
@Volatile private var isUploadComplete = false
@Volatile private var uploadSuccess = false
@Volatile private var uploadCancelled = false
```

2. Create an atomic completion handler:
```kotlin
private fun completeUpload(
    continuation: Continuation<Result?>,
    result: Result?,
    isSuccess: Boolean,
    error: Exception? = null
) {
    try {
        synchronized(this) {
            if (continuation is CancellableContinuation && continuation.isActive) {
                isUploadComplete = true
                uploadSuccess = isSuccess
                if (isSuccess && result != null) {
                    continuation.resume(result)
                } else if (error != null) {
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(null)
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error during completion", e)
        if (continuation is CancellableContinuation && continuation.isActive) {
            continuation.resumeWithException(e)
        }
    }
}
```

3. Key principles:
   - Use `@Volatile` for state variables to ensure visibility across threads
   - Make completion atomic with `synchronized` block
   - Check for `CancellableContinuation` and `isActive` before resuming
   - Set state before resuming continuation
   - Handle all completion scenarios (success, error, cancellation)
   - Use a single completion point to prevent race conditions

4. Usage:
```kotlin
override suspend fun uploadSomething(): Result? = suspendCancellableCoroutine { continuation ->
    // Reset state
    isUploadComplete = false
    uploadSuccess = false
    uploadCancelled = false
    
    // Set up cancellation handler first
    continuation.invokeOnCancellation { cause ->
        if (!isUploadComplete) {
            // Handle cancellation
            completeUpload(continuation, null, false)
        }
    }
    
    // Perform upload
    uploadTask
        .addOnSuccessListener { result ->
            if (!continuation.isCompleted) {
                completeUpload(continuation, result, true)
            }
        }
        .addOnFailureListener { e ->
            if (!continuation.isCompleted) {
                completeUpload(continuation, null, false, e)
            }
        }
}
```

This pattern ensures:
- Thread safety through synchronized blocks and @Volatile annotations
- Proper cancellation handling
- No duplicate completions
- Atomic state transitions
- Reliable completion even under network issues

## Key Technical Requirements
- Functions should be under 60 lines
- Files should be under 250 lines
- All code should be modular and testable
- Follow Material3 design principles

## Documentation Rules
- Update project-checklist.md after completing tasks
- Document failed approaches in deadends.md before trying new solutions
- Track progress percentages for each phase
- Document significant decisions and changes
- Flag any blockers or risks immediately

## Quick Links
- [Authentication Implementation](app/src/main/java/com/example/reel_ai/ui/auth)
- [Camera Implementation](app/src/main/java/com/example/reel_ai/ui/camera)
- [Data Layer](app/src/main/java/com/example/reel_ai/data)
- [Domain Layer](app/src/main/java/com/example/reel_ai/domain)
- [UI Layer](app/src/main/java/com/example/reel_ai/ui)
- [Dependency Injection](app/src/main/java/com/example/reel_ai/di)

## Notes
- This file should be referenced at the start of each new composer instance
- Use @entry to quickly access this documentation
- All linked documents should be kept up to date 
- Keep this file updated with the latest project status and next steps