package com.example.reel_ai.ui.feed;

import com.example.reel_ai.domain.repository.VideoRepository;
import com.example.reel_ai.util.VideoUploader;
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
public final class FeedViewModel_Factory implements Factory<FeedViewModel> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  private final Provider<VideoUploader> videoUploaderProvider;

  public FeedViewModel_Factory(Provider<VideoRepository> videoRepositoryProvider,
      Provider<VideoUploader> videoUploaderProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
    this.videoUploaderProvider = videoUploaderProvider;
  }

  @Override
  public FeedViewModel get() {
    return newInstance(videoRepositoryProvider.get(), videoUploaderProvider.get());
  }

  public static FeedViewModel_Factory create(Provider<VideoRepository> videoRepositoryProvider,
      Provider<VideoUploader> videoUploaderProvider) {
    return new FeedViewModel_Factory(videoRepositoryProvider, videoUploaderProvider);
  }

  public static FeedViewModel newInstance(VideoRepository videoRepository,
      VideoUploader videoUploader) {
    return new FeedViewModel(videoRepository, videoUploader);
  }
}
