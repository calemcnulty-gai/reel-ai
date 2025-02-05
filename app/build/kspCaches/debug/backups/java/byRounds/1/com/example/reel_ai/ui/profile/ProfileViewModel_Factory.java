package com.example.reel_ai.ui.profile;

import android.content.Context;
import com.example.reel_ai.domain.auth.AuthRepository;
import com.example.reel_ai.domain.repository.VideoRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<VideoRepository> videoRepositoryProvider;

  private final Provider<Context> contextProvider;

  public ProfileViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<VideoRepository> videoRepositoryProvider, Provider<Context> contextProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.videoRepositoryProvider = videoRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(authRepositoryProvider.get(), videoRepositoryProvider.get(), contextProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<VideoRepository> videoRepositoryProvider, Provider<Context> contextProvider) {
    return new ProfileViewModel_Factory(authRepositoryProvider, videoRepositoryProvider, contextProvider);
  }

  public static ProfileViewModel newInstance(AuthRepository authRepository,
      VideoRepository videoRepository, Context context) {
    return new ProfileViewModel(authRepository, videoRepository, context);
  }
}
