package com.stfalcon.imageviewer.viewer.view;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import com.stfalcon.imageviewer.common.extensions.ViewKt;

// TODO: 10/29/2019 internal
public final class TransitionImageAnimator {
  private static final long TRANSITION_DURATION_OPEN = 200L;
  private static final long TRANSITION_DURATION_CLOSE = 250L;

  // TODO: 10/29/2019 internal
  public boolean isAnimating;
  private boolean isClosing;
  private final ImageView externalImage;
  private final ImageView internalImage;
  private final FrameLayout internalImageContainer;

  public TransitionImageAnimator(@Nullable ImageView externalImage,
      @NonNull ImageView internalImage, @NonNull FrameLayout internalImageContainer) {
    this.externalImage = externalImage;
    this.internalImage = internalImage;
    this.internalImageContainer = internalImageContainer;
  }

  private long getTransitionDuration() {
    return isClosing ? TRANSITION_DURATION_CLOSE : TRANSITION_DURATION_OPEN;
  }

  private ViewGroup getInternalRoot() {
    return (ViewGroup) internalImageContainer.getParent();
  }

  // TODO: 10/29/2019 internal
  public void animateOpen(@NonNull int[] containerPadding,
      @NonNull Consumer<Long> onTransitionStart, @NonNull Runnable onTransitionEnd) {
    if (ViewKt.isRectVisible(externalImage)) {
      onTransitionStart.accept(TRANSITION_DURATION_OPEN);
      doOpenTransition(containerPadding, onTransitionEnd);
    } else {
      onTransitionEnd.run();
    }
  }

  // TODO: 10/29/2019 internal
  public void animateClose(boolean shouldDismissToBottom,
      @NonNull Consumer<Long> onTransitionStart, @NonNull Runnable onTransitionEnd) {
    if (ViewKt.isRectVisible(externalImage) && !shouldDismissToBottom) {
      onTransitionStart.accept(TRANSITION_DURATION_CLOSE);
      doCloseTransition(onTransitionEnd);
    } else {
      if (externalImage != null) {
        externalImage.setVisibility(View.VISIBLE);
      }

      onTransitionEnd.run();
    }
  }

  private void doOpenTransition(int[] containerPadding, Runnable onTransitionEnd) {
    isAnimating = true;
    prepareTransitionLayout();
    View internalRoot = getInternalRoot();
    internalRoot.post(() -> {
      //ain't nothing but a kludge to prevent blinking when transition is starting

      ImageView externalImage = this.externalImage;
      if (externalImage != null) {
        externalImage
            .postDelayed(() -> externalImage.setVisibility(View.INVISIBLE), 50L);
      }

      TransitionManager.beginDelayedTransition(getInternalRoot(), createTransition(() -> {
        if (!isClosing) {
          isAnimating = false;
          onTransitionEnd.run();
        }
      }));
      ViewKt.makeViewMatchParent(internalImageContainer);
      ViewKt.makeViewMatchParent(internalImage);
      ViewKt.applyMargin(internalRoot,
          containerPadding[0], containerPadding[1],
          containerPadding[2], containerPadding[3]
      );
      internalImageContainer.requestLayout();
    });
  }

  private void doCloseTransition(Runnable onTransitionEnd) {
    isAnimating = true;
    isClosing = true;
    TransitionManager.beginDelayedTransition(getInternalRoot(),
        createTransition(() -> handleCloseTransitionEnd(onTransitionEnd)));
    prepareTransitionLayout();
    internalImageContainer.requestLayout();
  }

  private void prepareTransitionLayout() {
    ImageView externalImage = this.externalImage;
    if (externalImage != null) {
      if (ViewKt.isRectVisible(externalImage)) {
        Rect rect = ViewKt.getLocalVisibleRect(externalImage);
        ViewKt.requestNewSize(internalImage, externalImage.getWidth(), externalImage.getHeight());
        ViewKt.applyMargin(internalImage, -rect.left, -rect.top, null, null);
        rect = ViewKt.getGlobalVisibleRect(externalImage);
        ViewKt.requestNewSize(internalImageContainer, rect.width(), rect.height());
        ViewKt.applyMargin(internalImageContainer, rect.left, rect.top, rect.right, rect.bottom);
      }

      resetRootTranslation();
    }
  }

  private void handleCloseTransitionEnd(Runnable onTransitionEnd) {
    if (externalImage != null) {
      externalImage.setVisibility(View.VISIBLE);
      externalImage.post(onTransitionEnd);
    }
    isAnimating = false;
  }

  private void resetRootTranslation() {
    getInternalRoot()
        .animate()
        .translationY(0f)
        .setDuration(getTransitionDuration())
        .start();
  }

  private Transition createTransition(Runnable onTransitionEnd) {
    return new AutoTransition()
        .setDuration(getTransitionDuration())
        .setInterpolator(new DecelerateInterpolator())
        .addListener(new TransitionListenerAdapter() {
          @Override public void onTransitionEnd(@NonNull Transition transition) {
            if (onTransitionEnd != null) {
              onTransitionEnd.run();
            }
          }
        });
  }
}
