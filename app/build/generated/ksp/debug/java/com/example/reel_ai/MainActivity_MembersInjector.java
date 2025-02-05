package com.example.reel_ai;

import com.example.reel_ai.ui.auth.GoogleAuthUiClient;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<GoogleAuthUiClient> googleAuthUiClientProvider;

  public MainActivity_MembersInjector(Provider<GoogleAuthUiClient> googleAuthUiClientProvider) {
    this.googleAuthUiClientProvider = googleAuthUiClientProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<GoogleAuthUiClient> googleAuthUiClientProvider) {
    return new MainActivity_MembersInjector(googleAuthUiClientProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectGoogleAuthUiClient(instance, googleAuthUiClientProvider.get());
  }

  @InjectedFieldSignature("com.example.reel_ai.MainActivity.googleAuthUiClient")
  public static void injectGoogleAuthUiClient(MainActivity instance,
      GoogleAuthUiClient googleAuthUiClient) {
    instance.googleAuthUiClient = googleAuthUiClient;
  }
}
