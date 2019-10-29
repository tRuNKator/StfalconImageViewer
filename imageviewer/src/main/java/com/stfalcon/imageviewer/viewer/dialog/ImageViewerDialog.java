package com.stfalcon.imageviewer.viewer.dialog;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.stfalcon.imageviewer.R;
import com.stfalcon.imageviewer.listeners.OnImageChangeListener;
import com.stfalcon.imageviewer.viewer.builder.BuilderData;
import com.stfalcon.imageviewer.viewer.view.ImageViewerView;
import java.util.List;

// TODO: 10/29/2019 internal
@SuppressWarnings("WeakerAccess")
public final class ImageViewerDialog<T> {
  final AlertDialog dialog;
  final ImageViewerView<T> viewerView;
  boolean animateOpen;
  final BuilderData<T> builderData;

  public ImageViewerDialog(@NonNull Context context, @NonNull BuilderData<T> builderData) {
    this.builderData = builderData;
    viewerView = new ImageViewerView<>(context);
    animateOpen = true;
    setupViewerView();
    dialog = new AlertDialog.Builder(context, getDialogStyle())
        .setView(viewerView)
        .setOnKeyListener((dialog, keyCode, event) -> onDialogKeyEvent(keyCode, event))
        .create();
    dialog.setOnShowListener(dialog ->
        viewerView.open$imageviewer_release(builderData.transitionView, animateOpen));
    dialog.setOnDismissListener(dialog -> {
      if (builderData.onDismissListener != null) {
        builderData.onDismissListener.onDismiss();
      }
    });
  }

  int getDialogStyle() {
    return builderData.shouldStatusBarHide ? R.style.ImageViewerDialog_NoStatusBar
        : R.style.ImageViewerDialog_Default;
  }

  private void setupViewerView() {
    viewerView.setZoomingAllowed$imageviewer_release(builderData.isZoomingAllowed);
    viewerView.setSwipeToDismissAllowed$imageviewer_release(builderData.isSwipeToDismissAllowed);
    viewerView.setContainerPadding$imageviewer_release(builderData.containerPaddingPixels);
    viewerView.setImagesMargin$imageviewer_release(builderData.imageMarginPixels);
    viewerView.setOverlayView$imageviewer_release(builderData.overlayView);
    viewerView.setBackgroundColor(builderData.backgroundColor);
    viewerView.setImages$imageviewer_release(builderData.images, builderData.startPosition,
        builderData.imageLoader);
    viewerView.setOnPageChange$imageviewer_release(position -> {
      OnImageChangeListener listener = builderData.imageChangeListener;
      if (listener != null) {
        listener.onImageChange(position);
      }
    });
    viewerView.setOnDismiss$imageviewer_release(dialog::dismiss);
  }

  public void show(boolean animate) {
    animateOpen = animate;
    dialog.show();
  }

  public void close() {
    viewerView.close$imageviewer_release();
  }

  public void updateImages(@NonNull List<T> images) {
    viewerView.updateImages$imageviewer_release(images);
  }

  public int getCurrentPosition() {
    return viewerView.getCurrentPosition$imageviewer_release();
  }

  public void updateTransitionImage(@Nullable ImageView imageView) {
    viewerView.updateTransitionImage$imageviewer_release(imageView);
  }

  boolean onDialogKeyEvent(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK
        && event.getAction() == KeyEvent.ACTION_UP
        && !event.isCanceled()) {
      if (viewerView.isScaled$imageviewer_release()) {
        viewerView.resetScale$imageviewer_release();
      } else {
        viewerView.close$imageviewer_release();
      }
    }

    return true;
  }
}

