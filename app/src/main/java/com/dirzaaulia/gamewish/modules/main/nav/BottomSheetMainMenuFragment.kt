//package com.dirzaaulia.gamewish.modules.main.nav
//
//import android.content.res.ColorStateList
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.annotation.MenuRes
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Observer
//import com.dirzaaulia.gamewish.R
//import com.dirzaaulia.gamewish.databinding.FragmentBottomSheetMainMenuBinding
//import com.dirzaaulia.gamewish.modules.main.adapter.BottomSheetMenuAdapter
//import com.dirzaaulia.gamewish.modules.main.model.MainMenuModel
//import com.dirzaaulia.gamewish.modules.main.model.MainMenuModelItem
//import com.dirzaaulia.gamewish.modules.main.nav.util.*
//import com.dirzaaulia.gamewish.util.themeColor
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.google.android.material.bottomsheet.BottomSheetBehavior.*
//import com.google.android.material.navigation.NavigationView
//import com.google.android.material.shape.MaterialShapeDrawable
//import kotlin.LazyThreadSafetyMode.NONE
//
//class BottomSheetMainMenuFragment : Fragment() {
//
//    private lateinit var binding : FragmentBottomSheetMainMenuBinding
//
//    private val behavior : BottomSheetBehavior<FrameLayout> by lazy(NONE) {
//            from(binding.backgroundContainer)
//    }
//
//    private val bottomSheetCallback = BottomSheetMenuCallback()
//
//    private val mainMenuListeners: MutableList<BottomSheetMenuAdapter.BottomSheetMenuAdapterListener>
//        = mutableListOf()
//
//    private val backgroundShapeDrawable: MaterialShapeDrawable by lazy(NONE) {
//        val backgroundContext = binding.backgroundContainer.context
//        MaterialShapeDrawable(
//                backgroundContext,
//                null,
//                R.attr.bottomSheetStyle,
//                0
//        ).apply {
//            fillColor = ColorStateList.valueOf(
//                backgroundContext.themeColor(R.attr.colorPrimarySurfaceVariant)
//            )
//            elevation = resources.getDimension(R.dimen.dimen_8dp)
//            initializeElevationOverlay(requireContext())
//        }
//    }
//
//    private val foregroundShapeDrawable: MaterialShapeDrawable by lazy(NONE) {
//        val foregroundContext = binding.foregroundContainer.context
//        MaterialShapeDrawable(
//                foregroundContext,
//                null,
//                R.attr.bottomSheetStyle,
//                0
//        ).apply {
//            fillColor = ColorStateList.valueOf(
//                    foregroundContext.themeColor(R.attr.colorPrimarySurface)
//            )
//            elevation = resources.getDimension(R.dimen.dimen_16dp)
//            shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_NEVER
//            initializeElevationOverlay(requireContext())
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        requireActivity().onBackPressedDispatcher.addCallback(this, closeMenuOnBackPressed)
//    }
//
//    @Suppress("DEPRECATION")
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = FragmentBottomSheetMainMenuBinding.inflate(inflater, container, false)
//        binding.foregroundContainer.setOnApplyWindowInsetsListener { _, insets ->
//            view?.setTag(
//                    R.id.tag_system_window_inset_top,
//                    insets.systemWindowInsetTop
//            )
//            insets
//        }
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.run {
//
//            backgroundContainer.background = backgroundShapeDrawable
//            foregroundContainer.background = foregroundShapeDrawable
//
//            scrimView.setOnClickListener { close() }
//
//            bottomSheetCallback.apply {
//                addOnSlideAction(AlphaSlideAction(scrimView))
//                addOnStateChangedAction(VisibilityStateAction(scrimView))
//
//                addOnSlideAction(ForegroundSheetTransformSlideAction(
//                        binding.foregroundContainer,
//                        foregroundShapeDrawable
//                ))
//
//                //addOnStateChangedAction(ScrollToTopStateAction(navView))
//                addOnStateChangedAction(object : OnStateChangedAction {
//                    override fun onStateChanged(sheet: View, newState: Int) {
//                        closeMenuOnBackPressed.isEnabled = newState != STATE_HIDDEN
//                    }
//                })
//            }
//
//            behavior.addBottomSheetCallback(bottomSheetCallback)
//            behavior.state = STATE_HIDDEN
//
//            foregroundContainer.menu.getItem(0).isChecked = true
//            foregroundContainer.setNavigationItemSelectedListener { item ->
//                when (item.itemId) {
//                    R.id.menu_search -> Toast.makeText(context, "Tes", Toast.LENGTH_SHORT).show()
//                }
//                true
//            }
//        }
//    }
//
//    private val closeMenuOnBackPressed = object : OnBackPressedCallback(false) {
//        override fun handleOnBackPressed() {
//            close()
//        }
//    }
//
//
//    fun toggle() {
//        when {
//            behavior.state == STATE_HIDDEN -> open()
//            behavior.state == STATE_HIDDEN
//                    || behavior.state == STATE_HALF_EXPANDED
//                    || behavior.state == STATE_EXPANDED
//                    || behavior.state == STATE_COLLAPSED -> close()
//        }
//    }
//
//    fun open() {
//        behavior.state = STATE_HALF_EXPANDED
//    }
//
//    fun close() {
//        behavior.state = STATE_HIDDEN
//    }
//
//    fun addOnSlideAction(action: OnSlideAction) {
//        bottomSheetCallback.addOnSlideAction(action)
//    }
//
//    fun addOnStateChangedAction(action: OnStateChangedAction) {
//        bottomSheetCallback.addOnStateChangedAction(action)
//    }
//
//    fun addNavigationListener(listener: BottomSheetMenuAdapter.BottomSheetMenuAdapterListener) {
//        mainMenuListeners.add(listener)
//    }
//
//
//
//}