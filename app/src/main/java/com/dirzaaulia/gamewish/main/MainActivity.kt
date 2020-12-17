package com.dirzaaulia.gamewish.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.math.MathUtils
import com.google.android.material.math.MathUtils.lerp
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.dirzaaulia.gamewish.main.nav.BottomSheetMainMenuFragment
import com.dirzaaulia.gamewish.main.nav.BottomSheetMenuCallback
import com.dirzaaulia.gamewish.main.nav.util.*
import com.dirzaaulia.gamewish.main.util.contentView
import com.dirzaaulia.gamewish.main.util.themeColor
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    private val bottomSheetMenu : BottomSheetMainMenuFragment by lazy(NONE) {
        supportFragmentManager.findFragmentById(R.id.bottom_sheet_main_menu) as BottomSheetMainMenuFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, closeMenuOnBackPressed)

        setupBottomAppBarAndFab()

    }

    private fun setupBottomAppBarAndFab() {
        binding.run {
            navController = this@MainActivity.findNavController(R.id.nav_host_fragment)
        }

        binding.fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
            setOnClickListener {
                //navigateToAddWishlist()
            }
        }

        bottomSheetMenu.apply {
            addOnSlideAction(FullClockwiseRotateSlideAction(binding.buttonMenu))
            addOnSlideAction(AlphaSlideAction(binding.bottomAppBarTitle, true))
            addOnStateChangedAction(ShowHideFabStateAction(binding.fab))
            addOnStateChangedAction(ChangeSettingsMenuStateAction { showSettings ->
                // Toggle between the current destination's BAB menu and the menu which should
                // be displayed when the BottomNavigationDrawer is open.
                binding.bottomAppBar.replaceMenu(if (showSettings) {
                    R.menu.main_menu
                } else {
                    R.menu.main_menu
                })
            })
        }

        binding.bottomAppBar.apply {
            setNavigationOnClickListener {
                bottomSheetMenu.toggle()
            }

            //setOnMenuItemClickListener(this@MainActivity)
        }

        binding.bottomAppBarContentContainer.setOnClickListener {
            bottomSheetMenu.toggle()
        }
    }

    private val closeMenuOnBackPressed = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            bottomSheetMenu.toggle()
        }
    }

}

//class MainActivity :
//    AppCompatActivity(),
//    Toolbar.OnMenuItemClickListener,
//    NavController.OnDestinationChangedListener,
//    BottomSheetMenuAdapter.BottomSheetMenuAdapterListener {
//
//    private val binding : ActivityMainBinding by contentView(R.layout.activity_main)
//    private val bottomSheetMainMenu: BottomSheetMainMenuFragment by lazy(NONE) {
//        supportFragmentManager.findFragmentById(R.id.bottom_sheet_main_menu) as BottomSheetMainMenuFragment
//    }
//
//    val currentNavigationFragment: Fragment?
//        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
//            ?.childFragmentManager
//            ?.fragments
//            ?.first()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setUpBottomNavigationAndFab()
//    }
//
//    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
//        when (destination.id) {
//            R.id.homeFragment -> {
//                setBottomAppBarForHome(getBottomAppBarMenuForDestination(destination))
//            }
//            R.id.dealsFragment -> {
//                //setBottomAppBarForDeals(getBottomAppBarMenuForDestination(destination))
//            }
//        }
//    }
//
//    override fun onMenuItemClicked(item: MainMenuModelItem.MainMenuItem) {
//        navigateToFragment(item.titleRes, item.directions)
//        if (item.id != 0) {
//            setBottomAppBarForDeals()
//        }
//        Log.i("MainActivity", item.id.toString())
//    }
//
//    override fun onMenuItemClick(item: MenuItem?): Boolean {
//        return true
//    }
//
//    private fun setUpBottomNavigationAndFab() {
//        binding.run {
//            findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(
//                this@MainActivity
//            )
//        }
//
//        binding.fab.apply {
//            setShowMotionSpecResource(R.animator.fab_show)
//            setHideMotionSpecResource(R.animator.fab_hide)
//            setOnClickListener {
//                navigateToAddWishlist()
//            }
//        }
//
//        bottomSheetMainMenu.apply {
//            addOnSlideAction(FullClockwiseRotateSlideAction(binding.buttonMenu))
//            addOnSlideAction(AlphaSlideAction(binding.bottomAppBarTitle, true))
//            addOnStateChangedAction(ShowHideFabStateAction(binding.fab))
//            addOnStateChangedAction(ChangeSettingsMenuStateAction{ showSettings ->
//                binding.bottomAppBar.replaceMenu(if (showSettings) {
//                    R.menu.main_menu
//                } else {
//                    getBottomAppBarMenuForDestination()
//                })
//            })
//
//            addNavigationListener(this@MainActivity)
//        }
//
//        binding.bottomAppBar.apply {
//            setNavigationOnClickListener {
//                bottomSheetMainMenu.toggle()
//            }
//
//            setOnMenuItemClickListener(this@MainActivity)
//        }
//
//        binding.bottomAppBarContentContainer.setOnClickListener {
//            bottomSheetMainMenu.toggle()
//        }
//    }
//
//    @MenuRes
//    private fun getBottomAppBarMenuForDestination(destination: NavDestination? = null): Int {
//        val dest = destination ?: findNavController(R.id.nav_host_fragment).currentDestination
//        return when (dest?.id) {
//            R.id.homeFragment -> R.menu.main_menu
//            R.id.dealsFragment -> R.menu.main_menu
//            else -> R.menu.main_menu
//        }
//    }
//
//    private fun setBottomAppBarForHome(@MenuRes menuRes: Int) {
//        binding.run {
//            fab.setImageState(intArrayOf(-android.R.attr.state_activated), true)
//            bottomAppBar.visibility = View.VISIBLE
//            bottomAppBar.replaceMenu(menuRes)
//            bottomAppBarTitle.visibility = View.VISIBLE
//            bottomAppBar.performShow()
//            fab.show()
//        }
//    }
//
//    private fun setBottomAppBarForDeals() {
//       binding.fab.visibility = View.GONE
//    }
//
//
//    fun navigateToFragment(@StringRes titleRes: Int, directions: NavDirections) {
//        binding.bottomAppBarTitle.text = getString(titleRes)
//        currentNavigationFragment?.apply {
//            exitTransition = MaterialFadeThrough().apply {
//                duration = 300.toLong()
//            }
//        }
//
//        findNavController(R.id.nav_host_fragment).navigate(directions)
//    }
//
//    fun navigateToDealsFragment(@StringRes titleRes: Int) {
//        binding.bottomAppBarTitle.text = getString(titleRes)
//        currentNavigationFragment?.apply {
//            exitTransition = MaterialFadeThrough().apply {
//                duration = 300.toLong()
//            }
//        }
//
//        val directions = HomeFragmentDirections.actionHomeFragmentToDealsFragment()
//        findNavController(R.id.nav_host_fragment).navigate(directions)
//    }
//
//    fun navigateToAddWishlist() {
//        currentNavigationFragment?.apply {
//            exitTransition = MaterialElevationScale(false).apply {
//                duration = 300.toLong()
//            }
//
//            reenterTransition = MaterialElevationScale(true).apply {
//                duration = 300.toLong()
//            }
//        }
//
////        val directions = ComposeFragmentDirections.actionGlobalComposeFragment(currentEmailId)
////        findNavController(R.id.nav_host_fragment).navigate(directions)
//    }
//}