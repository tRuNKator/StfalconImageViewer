package com.stfalcon.imageviewer.common.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public final class Views {
  @NonNull
  public static Rect getLocalVisibleRect(@Nullable View view) {
    Rect rect = new Rect();
    if (view != null) {
      view.getLocalVisibleRect(rect);
    }
    return rect;
  }

  @NonNull
  public static Rect getGlobalVisibleRect(@Nullable View view) {
    Rect rect = new Rect();
    if (view != null) {
      view.getGlobalVisibleRect(rect);
    }
    return rect;
  }

  @NonNull
  public static Rect getHitRect(@Nullable View view) {
    Rect rect = new Rect();
    if (view != null) {
      view.getHitRect(rect);
    }
    return rect;
  }

  public static boolean isRectVisible(@Nullable View view) {
    return view != null && !getGlobalVisibleRect(view).equals(getLocalVisibleRect(view));
  }

  public static void applyMargin(@NonNull View view, @Nullable Integer start, @Nullable Integer top,
      @Nullable Integer end, @Nullable Integer bottom) {
    if (view.getLayoutParams() instanceof MarginLayoutParams) {
      MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
      if (VERSION.SDK_INT >= 17) {
        lp.setMarginStart(start != null ? start : lp.getMarginStart());
        lp.setMarginEnd(end != null ? end : lp.getMarginEnd());
      } else {
        lp.leftMargin = start != null ? start : lp.leftMargin;
        lp.rightMargin = end != null ? end : lp.rightMargin;
      }

      lp.topMargin = top != null ? top : lp.topMargin;
      lp.bottomMargin = bottom != null ? bottom : lp.bottomMargin;
      view.setLayoutParams(lp);
    }
  }

  public static void requestNewSize(@NonNull View view, int width, int height) {
    ViewGroup.LayoutParams lp = view.getLayoutParams();
    lp.width = width;
    lp.height = height;
    view.setLayoutParams(view.getLayoutParams());
  }

  public static void makeViewMatchParent(@NonNull View view) {
    applyMargin(view, 0, 0, 0, 0);
    requestNewSize(view, MATCH_PARENT, MATCH_PARENT);
  }

  public static void animateAlpha(@NonNull View view, @Nullable Float from, @Nullable Float to,
      long duration) {
    view.setAlpha(from != null ? from : 0f);
    view.clearAnimation();
    view.animate().alpha(to != null ? to : 0f)
        .setDuration(duration)
        .start();
  }

  public static void switchVisibilityWithAnimation(@NonNull View view) {
    boolean isVisible = view.getVisibility() == View.VISIBLE;
    float from = isVisible ? 1f : 0f;
    float to = isVisible ? 0f : 1f;
    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", from, to);
    animator.setDuration((long) ViewConfiguration.getDoubleTapTimeout());
    if (isVisible) {
      animator.addListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          view.setVisibility(View.GONE);
        }
      });
    } else {
      view.setVisibility(View.VISIBLE);
    }

    animator.start();
  }
}