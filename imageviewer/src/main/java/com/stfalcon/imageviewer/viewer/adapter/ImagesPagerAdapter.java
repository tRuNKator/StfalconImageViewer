package com.stfalcon.imageviewer.viewer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.github.chrisbanes.photoview.PhotoView;
import com.stfalcon.imageviewer.common.pager.RecyclingPagerAdapter;
import com.stfalcon.imageviewer.loader.ImageLoader;
import java.util.ArrayList;
import java.util.List;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public final class ImagesPagerAdapter<T>
    extends RecyclingPagerAdapter<ImagesPagerAdapter.ViewHolder> {
  private final Context context;
  private final ImageLoader<T> imageLoader;
  private final boolean isZoomingAllowed;

  private List<T> images;
  private final List<ViewHolder> holders = new ArrayList<>();

  public ImagesPagerAdapter(@NonNull Context context, @NonNull List<T> images,
      @NonNull ImageLoader<T> imageLoader, boolean isZoomingAllowed) {
    super();
    this.context = context;
    this.imageLoader = imageLoader;
    this.isZoomingAllowed = isZoomingAllowed;
    this.images = images;
  }

  public boolean isScaled(int position) {
    ViewHolder found = null;
    for (ViewHolder holder : holders) {
      if (holder.getPosition() == position) {
        found = holder;
        break;
      }
    }

    return found != null && found.isScaled();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    PhotoView photoView = new PhotoView(context);
    photoView.setEnabled(isZoomingAllowed);
    photoView.setOnViewDragListener(
        (dx, dy) -> photoView.setAllowParentInterceptOnEdge(photoView.getScale() == 1f));
    ViewHolder holder = new ViewHolder(photoView);
    holders.add(holder);
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull ImagesPagerAdapter.ViewHolder holder,
      int position) {
    holder.bind(position);
  }

  @Override
  public int getItemCount() {
    return images.size();
  }

  public void updateImages(@NonNull List<T> images) {
    this.images = images;
    notifyDataSetChanged();
  }

  public void resetScale(int position) {
    ViewHolder found = null;
    for (ViewHolder holder : holders) {
      if (holder.getPosition() == position) {
        found = holder;
        break;
      }
    }

    if (found != null) {
      found.resetScale();
    }
  }

  final class ViewHolder extends RecyclingPagerAdapter.ViewHolder {
    private final PhotoView photoView;

    public boolean isScaled() {
      return photoView.getScale() > 1.0F;
    }

    public void bind(int position) {
      setPosition(position);
      imageLoader.loadImage(photoView, images.get(position));
    }

    public void resetScale() {
      photoView.setScale(photoView.getMinimumScale(), true);
    }

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      photoView = (PhotoView) itemView;
    }
  }
}
