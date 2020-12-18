package com.dirzaaulia.gamewish.main

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.NavigationGraphDirections
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.dirzaaulia.gamewish.main.nav.util.*
import com.dirzaaulia.gamewish.util.contentView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomAppBarAndFab()
    }

    private fun setupBottomAppBarAndFab() {
        binding.run {
            navController = this@MainActivity.findNavController(R.id.nav_host_fragment)

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