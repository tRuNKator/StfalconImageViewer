package com.stfalcon.imageviewer.common.gestures.direction;

import androidx.annotation.NonNull;

// TODO: 10/29/2019 internal
public enum SwipeDirection {
  NOT_DETECTED,
  UP,
  DOWN,
  LEFT,
  RIGHT;

  @NonNull
  public static SwipeDirection fromAngle(double angle) {
    return angle >= 0.0D && angle <= 45.0D ? SwipeDirection.RIGHT
        : (angle >= 45.0D && angle <= 135.0D ? SwipeDirection.UP
            : (angle >= 135.0D && angle <= 225.0D ? SwipeDirection.LEFT
                : (angle >= 225.0D && angle <= 315.0D ? SwipeDirection.DOWN
                    : (angle >= 315.0D && angle <= 360.0D ? SwipeDirection.RIGHT
                        : SwipeDirection.NOT_DETECTED))));
  }
}
