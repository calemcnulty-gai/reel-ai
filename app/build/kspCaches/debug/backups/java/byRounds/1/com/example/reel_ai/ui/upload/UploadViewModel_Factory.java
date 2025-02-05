package com.example.reel_ai.ui.upload;

import com.example.reel_ai.domain.repository.VideoRepository;
import com.example.reel_ai.domain.video.VideoManager;
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
public final class UploadViewModel_Factory implements Factory<UploadViewModel> {
  private final Provider<VideoManager> videoManagerProvider;

  private final Provider<VideoRepository> videoRepositoryProvider;

  public UploadViewModel_Factory(Provider<VideoManager> videoManagerProvider,
      Provider<VideoRepository> videoRepositoryProvider) {
    this.videoManagerProvider = videoManagerProvider;
    this.videoRepositoryProvider = videoRepositoryProvider;
  }

  @Override
  public UploadViewModel get() {
    return newInstance(videoManagerProvider.get(), videoRepositoryProvider.get());
  }

  public static UploadViewModel_Factory create(Provider<VideoManager> videoManagerProvider,
      Provider<VideoRepository> videoRepositoryProvider) {
    return new UploadViewModel_Factory(videoManagerProvider, videoRepositoryProvider);
  }

  public static UploadViewModel newInstance(VideoManager videoManager,
      VideoRepository videoRepository) {
    return new UploadViewModel(videoManager, videoRepository);
  }
}
