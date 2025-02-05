package com.example.reel_ai.util;

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
public final class VideoUploader_Factory implements Factory<VideoUploader> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  public VideoUploader_Factory(Provider<VideoRepository> videoRepositoryProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
  }

  @Override
  public VideoUploader get() {
    return newInstance(videoRepositoryProvider.get());
  }

  public static VideoUploader_Factory create(Provider<VideoRepository> videoRepositoryProvider) {
    return new VideoUploader_Factory(videoRepositoryProvider);
  }

  public static VideoUploader newInstance(VideoRepository videoRepository) {
    return new VideoUploader(videoRepository);
  }
}
