package com.example.reel_ai.domain.camera;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CameraManager_Factory implements Factory<CameraManager> {
  @Override
  public CameraManager get() {
    return newInstance();
  }

  public static CameraManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CameraManager newInstance() {
    return new CameraManager();
  }

  private static final class InstanceHolder {
    private static final CameraManager_Factory INSTANCE = new CameraManager_Factory();
  }
}
