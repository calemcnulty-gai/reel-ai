# Dead Ends: Failed Implementation Attempts

## Batch Thumbnail Generation

### 1. Simple Sequential Processing
**Attempt**: Process videos one at a time in a simple loop
**Why it failed**: 
- Blocked the main thread
- No progress updates
- No error isolation
- Single failure would stop entire process

### 2. Parallel Processing without Batching
**Attempt**: Launch all videos for processing at once using `async`
**Why it failed**:
- Overwhelmed device resources
- Too many concurrent Firebase operations
- Memory issues with multiple video downloads
- No control over system load

### 3. Firebase Functions
**Attempt**: Move thumbnail generation to Firebase Functions
**Why it failed**:
- Cold start latency too high
- Cost implications for video processing
- Network bandwidth issues transferring videos
- Complex error handling across services

### 4. WorkManager Implementation
**Attempt**: Use WorkManager for background processing
**Why it failed**:
- Lack of fine-grained control over batching
- Difficult to manage state across work sessions
- Complex coordination with foreground service
- Issues with progress reporting

### 5. Foreground Service with dataSync Type
**Attempt**: Use `android:foregroundServiceType="dataSync"` for the service
**Why it failed**:
- Android 14 restrictions on foreground services
- Permission issues (`FOREGROUND_SERVICE_DATA_SYNC`)
- User experience impact with persistent notification
- Battery optimization conflicts

### Successful Solution
The working solution uses:
1. Foreground service with `shortService` type
2. Coroutine Flow with buffer for controlled parallelism
3. Supervisor scope for error isolation
4. Batch size of 3 for resource management
5. Proper cleanup of temporary files
6. Progress tracking with broadcast updates
7. Timeout handling for Firestore operations 

## Duplicate Firebase Providers

### Initial Setup
**Attempt**: Provide Firebase dependencies in both `FirebaseModule` and `VideoModule`
**Why it failed**: 
- Hilt detected duplicate bindings for Firebase services
- Same dependencies (`FirebaseAuth`, `FirebaseStorage`, `FirebaseFirestore`) provided in two places
- Different initialization methods used (`Firebase.auth` vs `FirebaseAuth.getInstance()`)
- Build errors due to Hilt's inability to determine which provider to use

### Successful Solution
Consolidated Firebase providers by:
1. Keeping all Firebase-related bindings in `FirebaseModule`
2. Using KTX versions (`Firebase.auth`, etc.) as the single source of truth
3. Removing duplicate providers from `VideoModule`
4. Properly injecting Firebase dependencies into `VideoManager` 