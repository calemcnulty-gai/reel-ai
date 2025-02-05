package com.example.reel_ai.ui.preview;

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
public final class PreviewViewModel_Factory implements Factory<PreviewViewModel> {
  private final Provider<VideoManager> videoManagerProvider;

  public PreviewViewModel_Factory(Provider<VideoManager> videoManagerProvider) {
    this.videoManagerProvider = videoManagerProvider;
  }

  @Override
  public PreviewViewModel get() {
    return newInstance(videoManagerProvider.get());
  }

  public static PreviewViewModel_Factory create(Provider<VideoManager> videoManagerProvider) {
    return new PreviewViewModel_Factory(videoManagerProvider);
  }

  public static PreviewViewModel newInstance(VideoManager videoManager) {
    return new PreviewViewModel(videoManager);
  }
}
