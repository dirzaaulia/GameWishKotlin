package com.dirzaaulia.gamewish.main.nav

import android.annotation.SuppressLint
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.main.nav.util.OnSlideAction
import com.dirzaaulia.gamewish.main.nav.util.OnStateChangedAction
import com.dirzaaulia.gamewish.util.normalize
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.max

class BottomSheetMenuCallback : BottomSheetBehavior.BottomSheetCallback() {

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()
    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private var lastSlideOffset = -1.0F
    private var halfExpandedSlideOffset = Float.MAX_VALUE

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (halfExpandedSlideOffset == Float.MAX_VALUE) {
            calculateInitialHalfExpandedSlideOffset(bottomSheet)
        }

        lastSlideOffset = slideOffset

        val trueOffset = if (slideOffset <= halfExpandedSlideOffset) {
            slideOffset.normalize(-1F, halfExpandedSlideOffset, -1F, 0F)
        } else {
            slideOffset.normalize(halfExpandedSlideOffset, 1F, 0F, 1F)
        }

        onSlideActions.forEach { it.onSlide(bottomSheet, trueOffset) }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            halfExpandedSlideOffset = lastSlideOffset
            onSlide(bottomSheet, lastSlideOffset)
        }

        onStateChangedActions.forEach { it.onStateChanged(bottomSheet, newState) }
    }



    @SuppressLint("PrivateResource")
    private fun calculateInitialHalfExpandedSlideOffset(bottomSheet: View) {
        val parent = bottomSheet.parent as CoordinatorLayout
        val behavior = BottomSheetBehavior.from(bottomSheet)

        val halfExpandedOffset = parent.height * (1 - behavior.halfExpandedRatio)
        val peekHeightMin = parent.resources.getDimensionPixelSize(
                R.dimen.dimen_64dp
        )
        val peek = max(peekHeightMin, parent.height - parent.width * 9 / 16)
        val collapsedOffset = max(
                parent.height - peek,
                max(0, parent.height - bottomSheet.height)
        )

        halfExpandedSlideOffset =
                (collapsedOffset - halfExpandedOffset) / (parent.height - collapsedOffset)
    }

    fun addOnSlideAction(action: OnSlideAction) : Boolean {
        return onSlideActions.add(action)
    }

    fun removeOnSlideAction(action: OnSlideAction) : Boolean {
        return onSlideActions.remove(action)
    }

    fun addOnStateChangedAction(action: OnStateChangedAction) : Boolean {
        return onStateChangedActions.add(action)
    }

    fun removeOnStateChangedAction(action: OnStateChangedAction) : Boolean {
        return onStateChangedActions.remove(action)
    }
}