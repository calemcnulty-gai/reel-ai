package com.example.reel_ai.ui.video;

import com.example.reel_ai.domain.repository.VideoRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class VideoViewModel_Factory implements Factory<VideoViewModel> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  public VideoViewModel_Factory(Provider<VideoRepository> videoRepositoryProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
  }

  @Override
  public VideoViewModel get() {
    return newInstance(videoRepositoryProvider.get());
  }

  public static VideoViewModel_Factory create(Provider<VideoRepository> videoRepositoryProvider) {
    return new VideoViewModel_Factory(videoRepositoryProvider);
  }

  public static VideoViewModel newInstance(VideoRepository videoRepository) {
    return new VideoViewModel(videoRepository);
  }
}
