@file:Suppress("DEPRECATION")

package com.dirzaaulia.gamewish.util

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Platforms
import com.dirzaaulia.gamewish.modules.details.adapter.DetailsPlatformsAdapter
import com.dirzaaulia.gamewish.modules.search.adapter.SearchGamesPlatformsAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber
import vas.com.currencyconverter.CurrencyConverter
import java.util.*

@BindingAdapter("getSubReddit")
fun TextView.getSubReddit(value: String?) {

    if (value?.isNotEmpty() == true) {
        val uri = Uri.parse(value)
        val segment = uri.path!!.split("/")
        val subReddit = String.format("<u>r/%s</u>", segment[segment.size-2])
        text = htmlToTextFormatter(subReddit)
    }
}

@BindingAdapter("htmlToText")
fun TextView.htmlToText(value: String?) {

    text = if (value.isNullOrEmpty()) {
        ""
    } else {
        htmlToTextFormatter(value)
    }
}

@BindingAdapter("setDetailsPlatforms")
fun RecyclerView.setDetailsPlatforms(platforms: List<Platforms>?) {
    val platformsAdapter = DetailsPlatformsAdapter()
    platformsAdapter.submitList(platforms)
    adapter = platformsAdapter
}

@BindingAdapter("setPlatforms")
fun RecyclerView.setPlatforms(platforms: List<Platforms>) {
    val platformsAdapter = SearchGamesPlatformsAdapter()
    platformsAdapter.submitList(platforms)
    adapter = platformsAdapter
}

@BindingAdapter("textFormatReleaseDate")
fun TextView.textReleaseDate(value: String?) {
    value?.let {
        text = String.format("Release Date : ${textDateFormatter(value)}")
    }
}

@BindingAdapter("textDateFormatted")
fun TextView.textDateFormatted(value: String?) {
    value?.let {
        text = textDateFormatter2(value)
    }
}

@BindingAdapter("textCurrencyFormatted")
fun TextView.textCurrencyFormatted(value: String?) {
    value?.let{
        CurrencyConverter.calculate(
            value.toDouble(),
            Currency.getInstance("USD"),
            Currency.getInstance(Locale.getDefault())
        ) { value, e ->
            if (e != null) {
                Timber.i(e.localizedMessage)
            } else {
                text = currencyFormatter(value)
            }
        }
    }
}

@BindingAdapter("strikeThrough")
fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
    if (strikeThrough) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, it: Any?) {
    view.visibility = if (it == null || it == "" || it == 0) GONE else VISIBLE
}

/**
 * Binding adapter used to hide the spinner once data is available
 */
@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?) {
    view.visibility = if (it != null) GONE else VISIBLE
}

/**
 * Binding adapter used to display images from URL using Glide
 */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()

        val circularProgressDrawable = CircularProgressDrawable(imgView.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(imgView.context, R.color.color_on_surface_emphasis_high))
        circularProgressDrawable.start()

        Glide.with(imgView.context)
            .load(imgUri)
            .placeholder(circularProgressDrawable)
            .error(R.drawable.ic_baseline_broken_image_24)
            .into(imgView)
    }
}

@BindingAdapter("esrbRatingImage")
fun setImageBasedOnString(imgView: ImageView, value: String?) {
    value?.let {
        when (it) {
            "Everyone" -> {
                imgView.setImageResource(R.drawable.image_esrb_rating_everyone)
            }
            "Everyone 10+" -> {
                imgView.setImageResource(R.drawable.image_esrb_rating_everyone10)
            }
            "Teen" -> {
                imgView.setImageResource(R.drawable.image_esrb_rating_teen)
            }
            "Mature" -> {
                imgView.setImageResource(R.drawable.image_esrb_rating_mature)
            }
            "Adults Only" -> {
                imgView.setImageResource(R.drawable.image_esrb_rating_adults_only)
            }
        }
    }
}

@BindingAdapter(
    "popupElevationOverlay"
)
fun Spinner.bindPopupElevationOverlay(popupElevationOverlay: Float) {
    setPopupBackgroundDrawable(ColorDrawable(
        ElevationOverlayProvider(context)
            .compositeOverlayWithThemeSurfaceColorIfNeeded(popupElevationOverlay)
    ))
}

@BindingAdapter(
    "drawableStart",
    "drawableLeft",
    "drawableTop",
    "drawableEnd",
    "drawableRight",
    "drawableBottom",
    requireAll = false
)
fun TextView.bindDrawables(
    @DrawableRes drawableStart: Int? = null,
    @DrawableRes drawableLeft: Int? = null,
    @DrawableRes drawableTop: Int? = null,
    @DrawableRes drawableEnd: Int? = null,
    @DrawableRes drawableRight: Int? = null,
    @DrawableRes drawableBottom: Int? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(
        context.getDrawableOrNull(drawableStart ?: drawableLeft),
        context.getDrawableOrNull(drawableTop),
        context.getDrawableOrNull(drawableEnd ?: drawableRight),
        context.getDrawableOrNull(drawableBottom)
    )
}

/**
 * Set a Chip's leading icon using Glide.
 *
 * Optionally set the image to be center cropped and/or cropped to a circle.
 */
@BindingAdapter(
    "glideChipIcon",
    "glideChipIconCenterCrop",
    "glideChipIconCircularCrop",
    requireAll = false
)
fun Chip.bindGlideChipSrc(
    @DrawableRes drawableRes: Int?,
    centerCrop: Boolean = false,
    circularCrop: Boolean = false
) {
    if (drawableRes == null) return

    createGlideRequest(
        context,
        drawableRes,
        centerCrop,
        circularCrop
    ).listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean = true

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            chipIcon = resource
            return true
        }
    }).submit(
        resources.getDimensionPixelSize(R.dimen.dimen_32dp),
        resources.getDimensionPixelSize(R.dimen.dimen_32dp)
    )
}

@BindingAdapter(
    "glideSrc",
    "glideCenterCrop",
    "glideCircularCrop",
    requireAll = false
)
fun ImageView.bindGlideSrc(
    @DrawableRes drawableRes: Int?,
    centerCrop: Boolean = false,
    circularCrop: Boolean = false
) {
    if (drawableRes == null) return

    createGlideRequest(
        context,
        drawableRes,
        centerCrop,
        circularCrop
    ).into(this)
}

private fun createGlideRequest(
    context: Context,
    @DrawableRes src: Int,
    centerCrop: Boolean,
    circularCrop: Boolean
): RequestBuilder<Drawable> {
    val req = Glide.with(context).load(src)
    if (centerCrop) req.centerCrop()
    if (circularCrop) req.circleCrop()
    return req
}

@BindingAdapter("goneIf")
fun View.bindGoneIf(gone: Boolean) {
    visibility = if (gone) {
        GONE
    } else {
        VISIBLE
    }
}

@BindingAdapter("layoutFullscreen")
fun View.bindLayoutFullscreen(previousFullscreen: Boolean, fullscreen: Boolean) {
    if (previousFullscreen != fullscreen && fullscreen) {
        systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

@BindingAdapter(
    "paddingLeftSystemWindowInsets",
    "paddingTopSystemWindowInsets",
    "paddingRightSystemWindowInsets",
    "paddingBottomSystemWindowInsets",
    requireAll = false
)
fun View.applySystemWindowInsetsPadding(
    previousApplyLeft: Boolean,
    previousApplyTop: Boolean,
    previousApplyRight: Boolean,
    previousApplyBottom: Boolean,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean
) {
    if (previousApplyLeft == applyLeft &&
        previousApplyTop == applyTop &&
        previousApplyRight == applyRight &&
        previousApplyBottom == applyBottom
    ) {
        return
    }

    doOnApplyWindowInsets { view, insets, padding, _, _ ->
        val left = if (applyLeft) insets.systemWindowInsetLeft else 0
        val top = if (applyTop) insets.systemWindowInsetTop else 0
        val right = if (applyRight) insets.systemWindowInsetRight else 0
        val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

        view.setPadding(
            padding.left + left,
            padding.top + top,
            padding.right + right,
            padding.bottom + bottom
        )
    }
}

@BindingAdapter(
    "marginLeftSystemWindowInsets",
    "marginTopSystemWindowInsets",
    "marginRightSystemWindowInsets",
    "marginBottomSystemWindowInsets",
    requireAll = false
)
fun View.applySystemWindowInsetsMargin(
    previousApplyLeft: Boolean,
    previousApplyTop: Boolean,
    previousApplyRight: Boolean,
    previousApplyBottom: Boolean,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean
) {
    if (previousApplyLeft == applyLeft &&
        previousApplyTop == applyTop &&
        previousApplyRight == applyRight &&
        previousApplyBottom == applyBottom
    ) {
        return
    }

    doOnApplyWindowInsets { view, insets, _, margin, _ ->
        val left = if (applyLeft) insets.systemWindowInsetLeft else 0
        val top = if (applyTop) insets.systemWindowInsetTop else 0
        val right = if (applyRight) insets.systemWindowInsetRight else 0
        val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = margin.left + left
            topMargin = margin.top + top
            rightMargin = margin.right + right
            bottomMargin = margin.bottom + bottom
        }
    }
}

fun View.doOnApplyWindowInsets(
    block: (View, WindowInsets, InitialPadding, InitialMargin, Int) -> Unit
) {
    // Create a snapshot of the view's padding & margin states
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    val initialHeight = recordInitialHeightForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding & margin states
    setOnApplyWindowInsetsListener { v, insets ->
        block(v, insets, initialPadding, initialMargin, initialHeight)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun recordInitialMarginForView(view: View): InitialMargin {
    val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
        ?: throw IllegalArgumentException("Invalid view layout params")
    return InitialMargin(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
}

private fun recordInitialHeightForView(view: View): Int {
    return view.layoutParams.height
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}