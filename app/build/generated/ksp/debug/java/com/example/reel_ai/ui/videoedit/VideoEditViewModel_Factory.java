package com.example.reel_ai.ui.videoedit;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.example.reel_ai.domain.repository.VideoRepository;
import com.example.reel_ai.domain.video.VideoManager;
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
public final class VideoEditViewModel_Factory implements Factory<VideoEditViewModel> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  private final Provider<VideoManager> videoManagerProvider;

  private final Provider<Context> contextProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public VideoEditViewModel_Factory(Provider<VideoRepository> videoRepositoryProvider,
      Provider<VideoManager> videoManagerProvider, Provider<Context> contextProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
    this.videoManagerProvider = videoManagerProvider;
    this.contextProvider = contextProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public VideoEditViewModel get() {
    return newInstance(videoRepositoryProvider.get(), videoManagerProvider.get(), contextProvider.get(), savedStateHandleProvider.get());
  }

  public static VideoEditViewModel_Factory create(Provider<VideoRepository> videoRepositoryProvider,
      Provider<VideoManager> videoManagerProvider, Provider<Context> contextProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new VideoEditViewModel_Factory(videoRepositoryProvider, videoManagerProvider, contextProvider, savedStateHandleProvider);
  }

  public static VideoEditViewModel newInstance(VideoRepository videoRepository,
      VideoManager videoManager, Context context, SavedStateHandle savedStateHandle) {
    return new VideoEditViewModel(videoRepository, videoManager, context, savedStateHandle);
  }
}
