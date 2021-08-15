package com.dirzaaulia.gamewish.modules.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentSearchBinding
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupBottomNav()

        return binding.root
    }

    private fun setupBottomNav() {
        binding.apply {
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.search_fragment_container) as NavHostFragment
            val navController = navHostFragment.navController
            searchBottomNav.setupWithNavController(navController)
        }
    }
}