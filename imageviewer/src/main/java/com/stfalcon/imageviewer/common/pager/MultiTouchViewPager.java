package com.stfalcon.imageviewer.common.pager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

// TODO: 10/29/2019 internal
public final class MultiTouchViewPager extends ViewPager {
  private boolean isIdle = true;
  private boolean isInterceptionDisallowed;
  private OnPageChangeListener pageChangeListener;

  public MultiTouchViewPager(@NonNull Context context) {
    super(context);
  }

  public MultiTouchViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    pageChangeListener = new SimpleOnPageChangeListener() {
      @Override public void onPageScrollStateChanged(int state) {
        isIdle = state == ViewPager.SCROLL_STATE_IDLE;
      }
    };
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (pageChangeListener != null) {
      removeOnPageChangeListener(pageChangeListener);
    }
  }

  @Override
  public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    isInterceptionDisallowed = disallowIntercept;
    super.requestDisallowInterceptTouchEvent(disallowIntercept);
  }

  @Override
  public boolean dispatchTouchEvent(@NotNull MotionEvent ev) {
    boolean handled;
    if (ev.getPointerCount() > 1 && isInterceptionDisallowed) {
      requestDisallowInterceptTouchEvent(false);
      handled = super.dispatchTouchEvent(ev);
      requestDisallowInterceptTouchEvent(true);
    } else {
      handled = super.dispatchTouchEvent(ev);
    }

    return handled;
  }

  @Override
  public boolean onInterceptTouchEvent(@NotNull MotionEvent ev) {
    boolean handled;
    if (ev.getPointerCount() > 1) {
      handled = false;
    } else {
      try {
        handled = super.onInterceptTouchEvent(ev);
      } catch (IllegalArgumentException e) {
        handled = false;
      }
    }

    return handled;
  }

  @SuppressLint({"ClickableViewAccessibility"})
  @Override
  public boolean onTouchEvent(@NotNull MotionEvent ev) {
    boolean handled;
    try {
      handled = super.onTouchEvent(ev);
    } catch (IllegalArgumentException var4) {
      handled = false;
    }

    return handled;
  }

  public boolean isIdle() {
    return isIdle;
  }
}
