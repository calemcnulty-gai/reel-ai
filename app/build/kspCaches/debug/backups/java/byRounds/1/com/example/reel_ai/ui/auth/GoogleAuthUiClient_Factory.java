package com.example.reel_ai.ui.auth;

import android.content.Context;
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
public final class GoogleAuthUiClient_Factory implements Factory<GoogleAuthUiClient> {
  private final Provider<Context> contextProvider;

  public GoogleAuthUiClient_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GoogleAuthUiClient get() {
    return newInstance(contextProvider.get());
  }

  public static GoogleAuthUiClient_Factory create(Provider<Context> contextProvider) {
    return new GoogleAuthUiClient_Factory(contextProvider);
  }

  public static GoogleAuthUiClient newInstance(Context context) {
    return new GoogleAuthUiClient(context);
  }
}
