package com.dirzaaulia.gamewish.modules.home

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.dirzaaulia.gamewish.modules.home.listener.NavigationIconClickListener
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.homeToolbar)

        setupToolbar()

        binding.homeNavMenu.navMenuDeals.setOnClickListener {
            view?.findNavController()?.navigate(
                HomeFragmentDirections.actionGlobalDealsFragment()
            )
        }

        //binding.homeContainerScrollView.background = ContextCompat.getDrawable(requireContext() ,R.drawable.shr_product_grid_background_shape)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                //navigateToWithZSharedAxisAnimation()
                true
            }
            else -> false
        }
    }

    private fun setupToolbar() {
        binding.homeToolbar.setNavigationOnClickListener(
            NavigationIconClickListener(
                requireActivity(),
                binding.homeContainerScrollView,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_menu_24), // Menu open icon
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_close_24))
        )
    }

    private fun navigateTo(direction: NavDirections) {
        view?.findNavController()?.navigate(direction)
    }

    private fun navigateToWithZSharedAxisAnimation(direction: NavDirections) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        view?.findNavController()?.navigate(direction)
    }
}