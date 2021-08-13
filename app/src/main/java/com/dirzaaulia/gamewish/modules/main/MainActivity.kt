package com.dirzaaulia.gamewish.modules.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.UserPreferences
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.dirzaaulia.gamewish.util.DATA_STORE_FILE_NAME
import com.dirzaaulia.gamewish.util.ProtoSerializer
import com.dirzaaulia.gamewish.util.contentView
import dagger.hilt.android.AndroidEntryPoint

// Build the DataStore
private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ProtoSerializer,
)

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
                        R.id.homeFragment, R.id.dealsFragment, R.id.aboutFragment -> bottomNav.isVisible = true
                        else -> bottomNav.isVisible = false
                    }
                }
            }
        }
    }
}