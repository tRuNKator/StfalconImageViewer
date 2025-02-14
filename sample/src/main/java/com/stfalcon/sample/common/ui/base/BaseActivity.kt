package com.stfalcon.sample.common.ui.base

import android.os.Build
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.stfalcon.sample.R
import com.stfalcon.sample.common.extensions.getDrawableCompat
import com.stfalcon.sample.common.extensions.loadImage
import com.stfalcon.sample.common.models.Poster

abstract class BaseActivity : AppCompatActivity() {

    protected fun loadPosterImage(imageView: ImageView, poster: Poster?) {
        loadImage(imageView, poster?.url)
    }

    protected fun loadImage(imageView: ImageView, url: String?) {
        imageView.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background = getDrawableCompat(R.drawable.shape_placeholder)
            } else {
                setBackgroundDrawable(getDrawableCompat(R.drawable.shape_placeholder))
            }
            loadImage(url)
        }
    }
}