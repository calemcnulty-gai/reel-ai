package com.example.reel_ai.data.repository;

import android.content.Context;
import com.example.reel_ai.domain.video.VideoManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class VideoRepositoryImpl_Factory implements Factory<VideoRepositoryImpl> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<VideoManager> videoManagerProvider;

  private final Provider<Context> contextProvider;

  public VideoRepositoryImpl_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseStorage> storageProvider,
      Provider<VideoManager> videoManagerProvider, Provider<Context> contextProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
    this.storageProvider = storageProvider;
    this.videoManagerProvider = videoManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public VideoRepositoryImpl get() {
    return newInstance(authProvider.get(), firestoreProvider.get(), storageProvider.get(), videoManagerProvider.get(), contextProvider.get());
  }

  public static VideoRepositoryImpl_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseStorage> storageProvider,
      Provider<VideoManager> videoManagerProvider, Provider<Context> contextProvider) {
    return new VideoRepositoryImpl_Factory(authProvider, firestoreProvider, storageProvider, videoManagerProvider, contextProvider);
  }

  public static VideoRepositoryImpl newInstance(FirebaseAuth auth, FirebaseFirestore firestore,
      FirebaseStorage storage, VideoManager videoManager, Context context) {
    return new VideoRepositoryImpl(auth, firestore, storage, videoManager, context);
  }
}
