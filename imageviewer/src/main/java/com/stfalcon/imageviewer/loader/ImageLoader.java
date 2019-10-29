package com.stfalcon.imageviewer.loader;

import android.widget.ImageView;

/**
 * Interface definition for a callback to be invoked when image should be loaded
 */
public interface ImageLoader<T> {
  /**
   * Fires every time when image object should be displayed in a provided {@link ImageView}
   *
   * @param imageView an {@link ImageView} object where the image should be loaded
   * @param image image data from which image should be loaded
   */
  void loadImage(ImageView imageView, T image);
}
