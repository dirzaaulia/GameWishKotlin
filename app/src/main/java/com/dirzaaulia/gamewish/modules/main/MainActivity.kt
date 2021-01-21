package com.dirzaaulia.gamewish.modules.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.dirzaaulia.gamewish.util.contentView
import com.dirzaaulia.gamewish.util.hide
import com.dirzaaulia.gamewish.util.show
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply{
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            bottomNav.setupWithNavController(navController)

            // Hide bottom nav on screens which don't require it
            lifecycleScope.launchWhenResumed {
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.homeFragment, R.id.dealsFragment -> bottomNav.show()
                        else -> bottomNav.hide()
                    }
                }
            }
        }
    }
}