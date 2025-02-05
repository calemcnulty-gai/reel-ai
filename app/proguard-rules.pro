# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.flow.**

# Hilt
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keep,allowobfuscation @interface dagger.hilt.android.**
-keep,allowobfuscation @interface javax.inject.**
-keep class javax.inject.** { *; }
-keep class dagger.hilt.android.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Coil
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**
-keepclassmembers class * {
    @coil.annotation.* <methods>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keepclassmembers class com.example.reel_ai.domain.model.** { *; }

# ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepclassmembers class androidx.media3.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class androidx.compose.** { *; }

# Keep our models
-keep class com.example.reel_ai.domain.model.** { *; }
-keep class com.example.reel_ai.data.model.** { *; }

# Keep Video related classes
-keep class com.example.reel_ai.domain.video.** { *; }
-keep class com.example.reel_ai.ui.videoedit.** { *; }
-keep class com.example.reel_ai.ui.video.** { *; }

# MediaMetadataRetriever
-keep class android.media.MediaMetadataRetriever { *; }