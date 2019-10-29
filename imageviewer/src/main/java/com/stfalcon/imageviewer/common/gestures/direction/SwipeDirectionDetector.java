package com.stfalcon.imageviewer.common.gestures.direction;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.util.Consumer;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public final class SwipeDirectionDetector {
  private final int touchSlop;
  private float startX;
  private float startY;
  private boolean isDetected;
  private final Consumer<SwipeDirection> onDirectionDetected;

  public SwipeDirectionDetector(@NonNull Context context,
      @NonNull Consumer<SwipeDirection> onDirectionDetected) {
    this.onDirectionDetected = onDirectionDetected;
    this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  public void handleTouchEvent(@NonNull MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startX = event.getX();
        startY = event.getY();
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (!isDetected) {
          onDirectionDetected.accept(SwipeDirection.NOT_DETECTED);
        }

        startY = 0.0F;
        startX = 0.0F;
        isDetected = false;
        break;
      case MotionEvent.ACTION_MOVE:
        if (!isDetected && getEventDistance(event) > (float) touchSlop) {
          isDetected = true;
          onDirectionDetected.accept(getDirection(startX, startY, event.getX(), event.getY()));
        }
        break;
    }
  }

  /**
   * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method returns the direction
   * that an arrow pointing from p1 to p2 would have.
   *
   * @param x1 the x position of the first point
   * @param y1 the y position of the first point
   * @param x2 the x position of the second point
   * @param y2 the y position of the second point
   * @return the direction
   */
  private SwipeDirection getDirection(float x1, float y1, float x2, float y2) {
    double angle = getAngle(x1, y1, x2, y2);
    return SwipeDirection.fromAngle(angle);
  }

  /**
   * Finds the angle between two points in the plane (x1,y1) and (x2, y2) The angle is measured with
   * 0/360 being the X-axis to the right, angles increase counter clockwise.
   *
   * @param x1 the x position of the first point
   * @param y1 the y position of the first point
   * @param x2 the x position of the second point
   * @param y2 the y position of the second point
   * @return the angle between two points
   */
  private double getAngle(float x1, float y1, float x2, float y2) {
    double rad = Math.atan2((double) (y1 - y2), (double) (x2 - x1)) + Math.PI;
    return (rad * (double) 180 / Math.PI + (double) 180) % (double) 360;
  }

  private float getEventDistance(MotionEvent ev) {
    float dx = ev.getX(0) - startX;
    float dy = ev.getY(0) - startY;
    return (float) Math.sqrt((double) (dx * dx + dy * dy));
  }
}
