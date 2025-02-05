package com.example.reel_ai.di;

import android.content.Context;
import com.example.reel_ai.domain.video.VideoManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class VideoModule_ProvideVideoManagerFactory implements Factory<VideoManager> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  public VideoModule_ProvideVideoManagerFactory(Provider<Context> contextProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    this.contextProvider = contextProvider;
    this.storageProvider = storageProvider;
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public VideoManager get() {
    return provideVideoManager(contextProvider.get(), storageProvider.get(), authProvider.get(), firestoreProvider.get());
  }

  public static VideoModule_ProvideVideoManagerFactory create(Provider<Context> contextProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    return new VideoModule_ProvideVideoManagerFactory(contextProvider, storageProvider, authProvider, firestoreProvider);
  }

  public static VideoManager provideVideoManager(Context context, FirebaseStorage storage,
      FirebaseAuth auth, FirebaseFirestore firestore) {
    return Preconditions.checkNotNullFromProvides(VideoModule.INSTANCE.provideVideoManager(context, storage, auth, firestore));
  }
}
