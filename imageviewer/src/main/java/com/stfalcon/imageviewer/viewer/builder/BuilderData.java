package com.stfalcon.imageviewer.viewer.builder;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.listeners.OnImageChangeListener;
import com.stfalcon.imageviewer.loader.ImageLoader;
import java.util.List;

public final class BuilderData<T> {
  public int backgroundColor;
  public int startPosition;
  @Nullable
  public OnImageChangeListener imageChangeListener;
  @Nullable
  public OnDismissListener onDismissListener;
  @Nullable
  public View overlayView;
  public int imageMarginPixels;
  @NonNull
  public int[] containerPaddingPixels;
  public boolean shouldStatusBarHide;
  public boolean isZoomingAllowed;
  public boolean isSwipeToDismissAllowed;
  @Nullable
  public ImageView transitionView;
  @NonNull
  public final List<T> images;
  @NonNull
  public final ImageLoader<T> imageLoader;

  public BuilderData(@NonNull List<T> images, @NonNull ImageLoader<T> imageLoader) {
    this.images = images;
    this.imageLoader = imageLoader;
    this.backgroundColor = Color.BLACK;
    this.containerPaddingPixels = new int[4];
    this.shouldStatusBarHide = true;
    this.isZoomingAllowed = true;
    this.isSwipeToDismissAllowed = true;
  }
}
