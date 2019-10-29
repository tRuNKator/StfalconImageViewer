package com.stfalcon.imageviewer.common.gestures.dismiss;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.stfalcon.imageviewer.common.BooleanSupplier;
import com.stfalcon.imageviewer.common.tools.Views;

public final class SwipeToDismissHandler implements OnTouchListener {
  private static final long ANIMATION_DURATION = 200L;

  private static final AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

  private final View swipeView;
  private final Runnable onDismiss;
  private final OnSwipeViewModed onSwipeViewMove;
  private final BooleanSupplier shouldAnimateDismiss;

  private int translationLimit;
  private boolean isTracking;
  private float startY;

  public SwipeToDismissHandler(@NonNull View swipeView, @NonNull Runnable onDismiss,
      @NonNull OnSwipeViewModed onSwipeViewMove, @NonNull BooleanSupplier shouldAnimateDismiss) {
    this.swipeView = swipeView;
    this.onDismiss = onDismiss;
    this.onSwipeViewMove = onSwipeViewMove;
    this.shouldAnimateDismiss = shouldAnimateDismiss;
    this.translationLimit = swipeView.getHeight() / 4;
  }

  @SuppressLint({"ClickableViewAccessibility"})
  @Override
  public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (Views.getHitRect(swipeView).contains((int) event.getX(), (int) event.getY())) {
          isTracking = true;
        }

        startY = event.getY();
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (isTracking) {
          isTracking = false;
          onTrackingEnd(v.getHeight());
        }

        return true;
      case MotionEvent.ACTION_MOVE:
        if (isTracking) {
          float translationY = event.getY() - startY;
          swipeView.setTranslationY(translationY);
          onSwipeViewMove.onSwipeViewModed(translationY, translationLimit);
        }

        return true;
      default:
        return false;
    }
  }

  public void initiateDismissToBottom() {
    animateTranslation((float) swipeView.getHeight(), ANIMATION_DURATION);
  }

  private void onTrackingEnd(int parentHeight) {
    float animateTo =
        swipeView.getTranslationY() < ((float) -translationLimit) ? -((float) parentHeight)
            : swipeView.getTranslationY() > (float) translationLimit ? (float) parentHeight
                : 0f;
    if (animateTo != 0f && !shouldAnimateDismiss.getAsBoolean()) {
      onDismiss.run();
    } else {
      animateTranslation(animateTo, ANIMATION_DURATION);
    }
  }

  private void animateTranslation(float translationTo, long duration) {
    ObjectAnimator animator = ObjectAnimator.ofFloat(swipeView, View.TRANSLATION_Y, translationTo)
        .setDuration(duration);
    animator.setInterpolator(accelerateInterpolator);
    animator.addUpdateListener(
        listener -> onSwipeViewMove.onSwipeViewModed(swipeView.getTranslationY(),
            translationLimit));
    animator.addListener(new AnimatorListenerAdapter() {
      public void onAnimationEnd(@Nullable Animator animation) {
        if (translationTo != 0f) {
          onDismiss.run();
        }
      }
    });
    animator.start();
  }
}
