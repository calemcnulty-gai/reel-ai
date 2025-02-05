# Project Overview

## Project Name
**TokTokClone** *(Creator Vertical MVP)*

## Introduction
This project is an Android-based TikTok clone focusing on the **creator vertical**. The primary features include:
- **Video Recording:** Capture video using CameraX.
- **Video Upload:** Upload recorded videos to Firebase Cloud Storage and store metadata in Firestore.
- **Video Playback:** Render a vertically swipable video feed using ExoPlayer (mimicking TikTok's behavior).

## Technology Stack

- **Programming Language:** Kotlin  
- **UI Framework:** Jetpack Compose  
- **Architecture:** MVVM with Clean Architecture principles  
- **Dependency Injection:** Hilt (Dagger-Hilt)  
- **Camera:** CameraX for video recording  
- **Video Playback:** ExoPlayer for video playing  
- **Vertical Swipe Navigation:** Accompanist Pager (for vertical swiping similar to TikTok)  
- **Backend Services:**  
  - **Authentication:** Firebase Auth  
  - **Storage:** Firebase Cloud Storage  
  - **Database:** Firestore

## Order of Operations

1. **Project Setup:**
   - Install Android Studio and create the project.
   - Enable Jetpack Compose within the project by editing the `build.gradle` file.
   - Configure Firebase by adding the `google-services.json` file and updating Gradle dependencies.
   - Set up Hilt for dependency injection.

2. **Implement Core Features:**
   - **CameraScreen (Video Recording):**
     - Integrate CameraX with Jetpack Compose to display a live camera preview.
     - Provide UI controls (a floating record button) to start/stop video recording.
     - Isolate CameraX interaction within a `CameraManager` to better manage side effects.
   
   - **UploadConfirmation (Video Upload):**
     - Create a review screen after recording the video.
     - On user confirmation, trigger the upload process.
     - Use a dedicated `UploadVideoUseCase` to handle uploading to Firebase Cloud Storage and saving video metadata to Firestore.
   
   - **VideoFeedScreen (Video Playback):**
     - Fetch video metadata from Firestore.
     - Display videos in a vertical feed with smooth swipe transitions using Accompanist Pager.
     - Integrate ExoPlayer within a custom composable to handle video playback.

3. **User Interaction & Flow:**
   - Ensure a seamless flow between screens using Jetpack Compose Navigation.
   - Utilize default Android permission dialogs for camera and storage access.
   - Implement basic error handling (e.g., success/failure prompts during upload).

4. **Timeline:**
   - **Proof of Concept (PoC):** Aim to have a working prototype within 48 hours.
     - Core functions must include video recording, simple upload, and playback capabilities.
   - **Final Submission:** Feature complete and polished by day 4, with additional improvements possible until day 6.

5. **Important Technical Decisions (ITDs):**
   - Use **Jetpack Compose** for rapid iterative development and modern UI practices.
   - Adopt **MVVM with Clean Architecture** to ensure a modular, testable, and extensible codebase.
   - Leverage **Firebase** for authentication, storage, and real-time data management.
   - Isolate side-effect operations (Firebase calls and CameraX API) within dedicated repositories/managers to maintain pure, deterministic application logic.

## Next Steps
- **Set up project structure:**  
  Create necessary folders such as `di`, `domain`, `data`, and `ui` as per the proposed architecture.
- **Begin with the CameraScreen implementation:**  
  Set up CameraX integration and compose UI for video recording.
- **Integrate Firebase and upload functionality:**  
  Configure and test the video upload process.
- **Implement video playback:**  
  Develop the vertical swipable feed using Accompanist Pager and ExoPlayer.
- **Iterate on UI/UX:**  
  Continuously refine the app's look & feel to closely mimic TikTok.

---
This document serves as the roadmap for the Week 1 deliverables and further extensions towards a fully featured creator vertical. All decisions are aligned with achieving a rapid yet maintainable MVP. 