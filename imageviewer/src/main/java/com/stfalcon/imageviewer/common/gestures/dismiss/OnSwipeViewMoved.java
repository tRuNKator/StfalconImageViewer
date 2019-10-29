package com.stfalcon.imageviewer.common.gestures.dismiss;

import androidx.annotation.RestrictTo;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public interface OnSwipeViewMoved {
  void onSwipeViewMoved(float translationY, int translationLimit);
}
