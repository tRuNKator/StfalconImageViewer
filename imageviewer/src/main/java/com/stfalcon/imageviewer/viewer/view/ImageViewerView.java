package com.stfalcon.imageviewer.viewer.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Consumer;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;
import com.stfalcon.imageviewer.R;
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirection;
import com.stfalcon.imageviewer.common.gestures.direction.SwipeDirectionDetector;
import com.stfalcon.imageviewer.common.gestures.dismiss.SwipeToDismissHandler;
import com.stfalcon.imageviewer.common.pager.MultiTouchViewPager;
import com.stfalcon.imageviewer.common.tools.Views;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.stfalcon.imageviewer.viewer.adapter.ImagesPagerAdapter;
import java.util.Collections;
import java.util.List;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public final class ImageViewerView<T> extends RelativeLayout {
  public boolean isZoomingAllowed = true;
  public boolean isSwipeToDismissAllowed = true;

  public @Nullable Runnable onDismiss;
  public @Nullable Consumer<Integer> onPageChange;

  public @NonNull int[] containerPadding = new int[] {0, 0, 0, 0};

  private @Nullable View overlayView;

  private final ViewGroup rootContainer;
  private final View backgroundView;
  private final ViewGroup dismissContainer;

  private final FrameLayout transitionImageContainer;
  private final ImageView transitionImageView;
  private @Nullable ImageView externalTransitionImageView;

  private final MultiTouchViewPager imagesPager;
  private @Nullable ImagesPagerAdapter<T> imagesAdapter;

  private final SwipeDirectionDetector directionDetector;
  private final GestureDetectorCompat gestureDetector;
  private final ScaleGestureDetector scaleDetector;
  private @NonNull SwipeToDismissHandler swipeDismissHandler;

  private boolean wasScaled;
  private boolean wasDoubleTapped;
  private boolean isOverlayWasClicked;
  private @Nullable SwipeDirection swipeDirection;

  private @NonNull List<T> images = Collections.emptyList();
  private @Nullable ImageLoader<T> imageLoader;
  private @NonNull TransitionImageAnimator transitionImageAnimator;

  private int startPosition;

  public ImageViewerView(Context context) {
    this(context, null);
  }

  public ImageViewerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ImageViewerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    View.inflate(context, R.layout.view_image_viewer, this);

    rootContainer = findViewById(R.id.rootContainer);
    backgroundView = findViewById(R.id.backgroundView);
    dismissContainer = findViewById(R.id.dismissContainer);

    transitionImageContainer = findViewById(R.id.transitionImageContainer);
    transitionImageView = findViewById(R.id.transitionImageView);

    imagesPager = findViewById(R.id.imagesPager);
    imagesPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override public void onPageSelected(int position) {
        if (externalTransitionImageView != null) {
          externalTransitionImageView.setVisibility(
              isAtStartPosition() ? View.INVISIBLE : View.VISIBLE);
        }

        if (onPageChange != null) {
          onPageChange.accept(position);
        }
      }
    });

    directionDetector =
        new SwipeDirectionDetector(context, direction -> swipeDirection = direction);
    gestureDetector = new GestureDetectorCompat(context,
        new GestureDetector.SimpleOnGestureListener() {
          @Override public boolean onSingleTapConfirmed(MotionEvent e) {
            if (imagesPager.isIdle()) {
              handleSingleTap(e, isOverlayWasClicked);
            }
            return false;
          }

          @Override public boolean onDoubleTap(MotionEvent e) {
            wasDoubleTapped = !isScaled();
            return false;
          }
        }
    );
    scaleDetector =
        new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener());
  }

  @Override
  public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
    if (overlayView != null
        && overlayView.getVisibility() == View.VISIBLE
        && overlayView.dispatchTouchEvent(event)) {
      return true;
    }

    //noinspection ConstantConditions
    if (transitionImageAnimator == null || transitionImageAnimator.isAnimating) {
      return true;
    }
    //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
    if (wasDoubleTapped
        && event.getAction() == MotionEvent.ACTION_MOVE
        && event.getPointerCount() == 1) {
      return true;
    }

    handleUpDownEvent(event);

    if (swipeDirection == null
        && (scaleDetector.isInProgress() || event.getPointerCount() > 1 || wasScaled)) {
      wasScaled = true;
      return imagesPager.dispatchTouchEvent(event);
    }
    return isScaled() ? super.dispatchTouchEvent(event) : handleTouchIfNotScaled(event);
  }

  @Override
  public void setBackgroundColor(int color) {
    findViewById(R.id.backgroundView).setBackgroundColor(color);
  }

  public void setImages(@NonNull List<T> images, int startPosition,
      @NonNull ImageLoader<T> imageLoader) {
    this.images = images;
    this.imageLoader = imageLoader;
    imagesAdapter = new ImagesPagerAdapter<>(getContext(), images, imageLoader, isZoomingAllowed);
    imagesPager.setAdapter(imagesAdapter);
    setStartPosition(startPosition);
  }

  public void open(@Nullable ImageView transitionImageView, boolean animate) {
    prepareViewsForTransition();

    externalTransitionImageView = transitionImageView;

    if (imageLoader != null) {
      imageLoader.loadImage(this.transitionImageView, images.get(startPosition));
    }

    copyBitmapFrom(this.transitionImageView, transitionImageView);
    transitionImageAnimator = createTransitionImageAnimator(transitionImageView);
    swipeDismissHandler = createSwipeToDismissHandler();

    rootContainer.setOnTouchListener(swipeDismissHandler);
    if (animate) {
      animateOpen();
    } else {
      prepareViewsForViewer();
    }
  }

  public void close() {
    if (isShouldDismissToBottom()) {
      swipeDismissHandler.initiateDismissToBottom();
    } else {
      animateClose();
    }
  }

  public void updateImages(@NonNull List<T> images) {
    this.images = images;
    if (imagesAdapter != null) {
      imagesAdapter.updateImages(images);
    }
  }

  public void updateTransitionImage(@Nullable ImageView imageView) {
    if (externalTransitionImageView != null) {
      externalTransitionImageView.setVisibility(View.VISIBLE);
    }

    if (imageView != null) {
      imageView.setVisibility(View.INVISIBLE);
    }

    externalTransitionImageView = imageView;
    setStartPosition(getCurrentPosition());
    transitionImageAnimator = createTransitionImageAnimator(imageView);
    if (imageLoader != null) {
      imageLoader.loadImage(transitionImageView, images.get(startPosition));
    }
  }

  public void resetScale() {
    if (imagesAdapter != null) {
      imagesAdapter.resetScale(getCurrentPosition());
    }
  }

  private void animateOpen() {
    transitionImageAnimator.animateOpen(containerPadding, duration -> {
      Views.animateAlpha(backgroundView, 0f, 1f, duration);
      if (overlayView != null) {
        Views.animateAlpha(overlayView, 0f, 1f, duration);
      }
    }, this::prepareViewsForViewer);
  }

  private void animateClose() {
    prepareViewsForTransition();
    Views.applyMargin(dismissContainer, 0, 0, 0, 0);

    transitionImageAnimator.animateClose(isShouldDismissToBottom(), duration -> {
      Views.animateAlpha(backgroundView, backgroundView.getAlpha(), 0f, duration);
      if (overlayView != null) {
        Views.animateAlpha(overlayView, overlayView.getAlpha(), 0f, duration);
      }
    }, () -> {
      if (onDismiss != null) {
        onDismiss.run();
      }
    });
  }

  private void prepareViewsForTransition() {
    transitionImageContainer.setVisibility(View.VISIBLE);
    imagesPager.setVisibility(View.GONE);
  }

  private void prepareViewsForViewer() {
    transitionImageContainer.setVisibility(View.GONE);
    imagesPager.setVisibility(View.VISIBLE);
  }

  private boolean handleTouchIfNotScaled(MotionEvent event) {
    directionDetector.handleTouchEvent(event);

    if (swipeDirection == null) {
      return true;
    }

    switch (swipeDirection) {
      case UP:
      case DOWN:
        if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle()) {
          return swipeDismissHandler.onTouch(rootContainer, event);
        } else {
          return true;
        }
      case LEFT:
      case RIGHT:
        return imagesPager.dispatchTouchEvent(event);
      default:
        return true;
    }
  }

  private void handleUpDownEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      handleEventActionUp(event);
    }

    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      handleEventActionDown(event);
    }

    scaleDetector.onTouchEvent(event);
    gestureDetector.onTouchEvent(event);
  }

  private void handleEventActionDown(MotionEvent event) {
    swipeDirection = null;
    wasScaled = false;
    imagesPager.dispatchTouchEvent(event);

    swipeDismissHandler.onTouch(rootContainer, event);
    isOverlayWasClicked = dispatchOverlayTouch(event);
  }

  private void handleEventActionUp(MotionEvent event) {
    wasDoubleTapped = false;

    swipeDismissHandler.onTouch(rootContainer, event);
    imagesPager.dispatchTouchEvent(event);
    isOverlayWasClicked = dispatchOverlayTouch(event);
  }

  private void handleSingleTap(MotionEvent event, boolean isOverlayWasClicked) {
    if (overlayView != null && !isOverlayWasClicked) {
      Views.switchVisibilityWithAnimation(overlayView);
      super.dispatchTouchEvent(event);
    }
  }

  private void handleSwipeViewMove(float translationY, int translationLimit) {
    float alpha = calculateTranslationAlpha(translationY, translationLimit);
    backgroundView.setAlpha(alpha);
    if (overlayView != null) {
      overlayView.setAlpha(alpha);
    }
  }

  private boolean dispatchOverlayTouch(MotionEvent event) {
    return overlayView != null && overlayView.getVisibility() == View.VISIBLE
        && overlayView.dispatchTouchEvent(event);
  }

  private float calculateTranslationAlpha(float translationY, int translationLimit) {
    return 1f - 1f / (float) translationLimit / 4f * Math.abs(translationY);
  }

  public int getCurrentPosition() {
    return imagesPager.getCurrentItem();
  }

  public void setCurrentPosition(int value) {
    imagesPager.setCurrentItem(value);
  }

  public boolean isScaled() {
    return imagesAdapter != null && imagesAdapter.isScaled(getCurrentPosition());
  }

  public int getImagesMargin() {
    return imagesPager.getPageMargin();
  }

  public void setImagesMargin(int value) {
    imagesPager.setPageMargin(value);
  }

  @Nullable
  public View getOverlayView() {
    return overlayView;
  }

  public void setOverlayView(@Nullable View value) {
    overlayView = value;
    if (value != null) {
      rootContainer.addView(value);
    }
  }

  private void setStartPosition(int value) {
    startPosition = value;
    setCurrentPosition(value);
  }

  private boolean isShouldDismissToBottom() {
    return externalTransitionImageView == null
        || !Views.isRectVisible(externalTransitionImageView)
        || !isAtStartPosition();
  }

  private boolean isAtStartPosition() {
    return getCurrentPosition() == startPosition;
  }

  private TransitionImageAnimator createTransitionImageAnimator(ImageView transitionImageView) {
    return new TransitionImageAnimator(transitionImageView, this.transitionImageView,
        this.transitionImageContainer);
  }

  private SwipeToDismissHandler createSwipeToDismissHandler() {
    return new SwipeToDismissHandler(dismissContainer, this::animateClose,
        this::handleSwipeViewMove, this::isShouldDismissToBottom);
  }

  private static void copyBitmapFrom(@NonNull ImageView dest, @Nullable ImageView target) {
    if (target != null) {
      Drawable drawable = target.getDrawable();
      if (drawable != null && drawable instanceof BitmapDrawable) {
        dest.setImageBitmap(((BitmapDrawable) drawable).getBitmap());
      }
    }
  }
}
