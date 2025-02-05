package com.example.reel_ai.ui.discard;

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
public final class DiscardViewModel_Factory implements Factory<DiscardViewModel> {
  private final Provider<VideoManager> videoManagerProvider;

  public DiscardViewModel_Factory(Provider<VideoManager> videoManagerProvider) {
    this.videoManagerProvider = videoManagerProvider;
  }

  @Override
  public DiscardViewModel get() {
    return newInstance(videoManagerProvider.get());
  }

  public static DiscardViewModel_Factory create(Provider<VideoManager> videoManagerProvider) {
    return new DiscardViewModel_Factory(videoManagerProvider);
  }

  public static DiscardViewModel newInstance(VideoManager videoManager) {
    return new DiscardViewModel(videoManager);
  }
}
