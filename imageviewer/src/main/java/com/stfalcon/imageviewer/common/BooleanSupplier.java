package com.stfalcon.imageviewer.common;

import androidx.annotation.RestrictTo;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public interface BooleanSupplier {

  /**
   * Gets a result.
   *
   * @return a result
   */
  boolean getAsBoolean();
}
