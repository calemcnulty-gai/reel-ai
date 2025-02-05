package com.example.reel_ai.data.service;

import com.example.reel_ai.domain.repository.VideoRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ThumbnailGenerationService_MembersInjector implements MembersInjector<ThumbnailGenerationService> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  public ThumbnailGenerationService_MembersInjector(
      Provider<VideoRepository> videoRepositoryProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
  }

  public static MembersInjector<ThumbnailGenerationService> create(
      Provider<VideoRepository> videoRepositoryProvider) {
    return new ThumbnailGenerationService_MembersInjector(videoRepositoryProvider);
  }

  @Override
  public void injectMembers(ThumbnailGenerationService instance) {
    injectVideoRepository(instance, videoRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.reel_ai.data.service.ThumbnailGenerationService.videoRepository")
  public static void injectVideoRepository(ThumbnailGenerationService instance,
      VideoRepository videoRepository) {
    instance.videoRepository = videoRepository;
  }
}
