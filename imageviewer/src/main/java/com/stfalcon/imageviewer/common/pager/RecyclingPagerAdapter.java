package com.stfalcon.imageviewer.common.pager;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.viewpager.widget.PagerAdapter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public abstract class RecyclingPagerAdapter<VH extends RecyclingPagerAdapter.ViewHolder>
    extends PagerAdapter {
  private static final String STATE = RecyclingPagerAdapter.class.getSimpleName();
  private static final int VIEW_TYPE_IMAGE = 0;

  private final SparseArray<RecycleCache> typeCaches = new SparseArray<>();
  private SparseArray<Parcelable> savedStates = new SparseArray<>();

  public abstract int getItemCount();

  @NonNull
  public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

  public abstract void onBindViewHolder(@NonNull VH holder, int position);

  @Override public void destroyItem(@NonNull ViewGroup parent, int position, @NonNull Object item) {
    if (item instanceof ViewHolder) {
      ((ViewHolder) item).detach(parent);
    }
  }

  @Override public int getCount() {
    return getItemCount();
  }

  @Override public int getItemPosition(@NonNull Object item) {
    return POSITION_NONE;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup parent, int position) {
    RecycleCache cache = typeCaches.get(VIEW_TYPE_IMAGE);
    if (cache == null) {
      cache = new RecycleCache(this);
      typeCaches.put(VIEW_TYPE_IMAGE, cache);
    }

    ViewHolder holder = cache.getFreeViewHolder(parent, VIEW_TYPE_IMAGE);
    holder.attach(parent, position);
    onBindViewHolder((VH) holder, position);
    holder.onRestoreInstanceState(savedStates.get(getItemId(position)));
    return holder;
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
    return obj instanceof ViewHolder && ((ViewHolder) obj).getItemView() == view;
  }

  @Override
  public Parcelable saveState() {
    for (ViewHolder viewHolder : getAttachedViewHolders()) {
      savedStates.put(getItemId(viewHolder.getPosition()), viewHolder.onSaveInstanceState());
    }

    Bundle state = new Bundle();
    state.putSparseParcelableArray(STATE, savedStates);
    return state;
  }

  @Override
  public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
    if (state != null && state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      bundle.setClassLoader(loader);
      if (bundle.containsKey(STATE)) {
        savedStates = bundle.getSparseParcelableArray(STATE);
      } else {
        savedStates = new SparseArray<>();
      }
    }

    super.restoreState(state, loader);
  }

  private int getItemId(int position) {
    return position;
  }

  private List<ViewHolder> getAttachedViewHolders() {
    ArrayList<ViewHolder> attachedViewHolders = new ArrayList<>();
    int size = typeCaches.size();
    for (int index = 0; index < size; ++index) {
      if (size != typeCaches.size()) {
        throw new ConcurrentModificationException();
      }

      for (ViewHolder holder : typeCaches.valueAt(index).getCaches()) {
        if (holder.isAttached()) {
          attachedViewHolders.add(holder);
        }
      }
    }

    return attachedViewHolders;
  }

  private static final class RecycleCache {
    @NonNull
    private final List<ViewHolder> caches = new ArrayList<>();
    private final RecyclingPagerAdapter<?> adapter;

    public RecycleCache(@NonNull RecyclingPagerAdapter<?> adapter) {
      this.adapter = adapter;
    }

    @NonNull
    public final ViewHolder getFreeViewHolder(@NonNull ViewGroup parent, int viewType) {
      int iterationsCount = 0;
      ViewHolder viewHolder;
      while (iterationsCount < caches.size()) {
        viewHolder = caches.get(iterationsCount);
        if (!viewHolder.isAttached) {
          return viewHolder;
        }
        iterationsCount++;
      }

      ViewHolder holder = adapter.onCreateViewHolder(parent, viewType);
      caches.add(holder);
      return holder;
    }

    @NonNull
    public final List<ViewHolder> getCaches() {
      return caches;
    }
  }

  @RestrictTo(value = RestrictTo.Scope.LIBRARY)
  public abstract static class ViewHolder {
    private static final String STATE = ViewHolder.class.getSimpleName();

    private int position;
    private boolean isAttached;
    @NonNull
    private final View itemView;

    public final int getPosition() {
      return position;
    }

    public final void setPosition(int var1) {
      position = var1;
    }

    public final boolean isAttached() {
      return isAttached;
    }

    public final void setAttached(boolean var1) {
      isAttached = var1;
    }

    public final void attach(@NonNull ViewGroup parent, int position) {
      isAttached = true;
      this.position = position;
      parent.addView(itemView);
    }

    public final void detach(@NonNull ViewGroup parent) {
      parent.removeView(itemView);
      isAttached = false;
    }

    public final void onRestoreInstanceState(@Nullable Parcelable state) {
      SparseArray<Parcelable> savedState = getStateFromParcelable(state);
      if (savedState != null) {
        itemView.restoreHierarchyState(savedState);
      }
    }

    @NonNull
    public final Parcelable onSaveInstanceState() {
      SparseArray<Parcelable> state = new SparseArray<>();
      itemView.saveHierarchyState(state);
      Bundle bundle = new Bundle();
      bundle.putSparseParcelableArray(STATE, state);
      return bundle;
    }

    private SparseArray<Parcelable> getStateFromParcelable(Parcelable state) {
      return state != null && state instanceof Bundle && ((Bundle) state).containsKey(STATE)
          ? ((Bundle) state).getSparseParcelableArray(STATE) : null;
    }

    @NonNull
    public final View getItemView() {
      return itemView;
    }

    public ViewHolder(@NonNull View itemView) {
      super();
      this.itemView = itemView;
    }
  }
}
