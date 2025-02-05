package com.example.reel_ai.di;

import android.content.Context;
import com.example.reel_ai.ui.auth.GoogleAuthUiClient;
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
public final class AppModule_ProvideGoogleAuthUiClientFactory implements Factory<GoogleAuthUiClient> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideGoogleAuthUiClientFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GoogleAuthUiClient get() {
    return provideGoogleAuthUiClient(contextProvider.get());
  }

  public static AppModule_ProvideGoogleAuthUiClientFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideGoogleAuthUiClientFactory(contextProvider);
  }

  public static GoogleAuthUiClient provideGoogleAuthUiClient(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGoogleAuthUiClient(context));
  }
}
