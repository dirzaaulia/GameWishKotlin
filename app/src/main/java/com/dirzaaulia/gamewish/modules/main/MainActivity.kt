package com.dirzaaulia.gamewish.modules.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.dirzaaulia.gamewish.NavigationGraphDirections
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.dirzaaulia.gamewish.modules.main.nav.util.*
import com.dirzaaulia.gamewish.util.contentView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
 NavController.OnDestinationChangedListener,
 BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomAppBarAndFab()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.homeFragment -> {
                setViewForHome()

            }
            R.id.dealsFragment -> {
                setViewForDeals()
            }
            R.id.searchFragment -> {
                setViewForSearch()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home -> {
                navigateTo(NavigationGraphDirections.actionGlobalHomeFragment())
                binding.bottomNavigationView.menu.forEach { it.isEnabled = true }
                binding.bottomNavigationView.menu[0].isEnabled = false
                true
            }
            R.id.menu_deals -> {
                navigateTo(NavigationGraphDirections.actionGlobalDealsFragment())
                binding.bottomNavigationView.menu.forEach { it.isEnabled = true }
                binding.bottomNavigationView.menu[1].isEnabled = false
                true
            }
            else -> false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun setupBottomAppBarAndFab() {
        binding.run {
            navController = findNavController(R.id.nav_host_fragment)

            navController.addOnDestinationChangedListener(this@MainActivity)

            appBarConfiguration = AppBarConfiguration
                .Builder(R.id.homeFragment, R.id.dealsFragment)
                .build()

            NavigationUI.setupWithNavController(bottomNavigationView, navController)

            bottomNavigationView.setOnNavigationItemSelectedListener(this@MainActivity)
        }

        binding.fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
            setOnClickListener {
                //navigateToAddWishlist()
            }
        }
    }

    private fun navigateTo(directions: NavDirections) {
        navController.navigate(directions)
    }



    private fun setViewForHome() {
        binding.run {
            bottomNavigationView.visibility = View.VISIBLE
            fab.show()
        }
    }

    private fun setViewForDeals() {
        binding.run {
            fab.hide()
        }
    }

    private fun setViewForSearch() {
        binding.run {
            //hideBottomNavView()
            bottomNavigationView.visibility = View.INVISIBLE
            bottomNavigationView.visibility = View.GONE
            fab.hide()
        }
    }

    private fun hideBottomNavView() {
        binding.run {
            bottomNavigationView.animate().setListener(object: AnimatorListenerAdapter() {
                var isCanceled = false
                override fun onAnimationEnd(animation: Animator?) {
                    if (isCanceled) return

                    // Hide the BottomAppBar to avoid it showing above the keyboard
                    // when composing a new email.
                    bottomNavigationView.visibility = View.INVISIBLE
                    fab.visibility = View.INVISIBLE
                }
                override fun onAnimationCancel(animation: Animator?) {
                    isCanceled = true
                }
            })
        }
    }
}