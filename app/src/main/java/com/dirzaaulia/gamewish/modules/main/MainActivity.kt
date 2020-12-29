package com.dirzaaulia.gamewish.modules.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
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
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomAppBarAndFab()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun setupBottomAppBarAndFab() {
        binding.run {
            navController = this@MainActivity.findNavController(R.id.nav_host_fragment)

            appBarConfiguration = AppBarConfiguration
                .Builder(R.id.homeFragment, R.id.dealsFragment)
                .build()

            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }

        binding.bottomNavigationView.apply {
            setOnNavigationItemSelectedListener { item: MenuItem ->
                this.menu.forEach { it.isEnabled = true }
                when (item.itemId) {
                    R.id.menu_home -> {
                        navigateTo(NavigationGraphDirections.actionGlobalHomeFragment())
                        item.isEnabled = false
                        binding.fab.show()
                        true
                    }
                    R.id.menu_deals -> {
                        navigateTo(NavigationGraphDirections.actionGlobalDealsFragment())
                        item.isEnabled = false
                        binding.fab.hide()
                        true
                    }
                    else -> false
                }
            }
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
}