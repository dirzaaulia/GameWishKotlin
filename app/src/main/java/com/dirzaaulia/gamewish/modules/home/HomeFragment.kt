package com.dirzaaulia.gamewish.modules.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.dirzaaulia.gamewish.modules.home.adapter.HomeAdapter
import com.dirzaaulia.gamewish.modules.main.MainActivity
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment :
    Fragment(),
    HomeAdapter.HomeAdapterListener {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: HomeViewModel by viewModels()
    private var adapter = HomeAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.homeToolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAdapter()
        subscribeWishlist(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                navigateToWithZSharedAxisAnimation(
                    HomeFragmentDirections.actionHomeFragmentToSearchFragment()
                )
                true
            }
            else -> false
        }
    }

    override fun onItemClicked(view: View, wishlist: Wishlist) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val gamesDetailTransitionName = getString(R.string.detail_transition_name)
        val extras = FragmentNavigatorExtras(view to gamesDetailTransitionName)
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(wishlist.id!!)
        view.findNavController().navigate(directions, extras)
    }

    private fun setupAdapter() {
        binding.homeRecyclerView.adapter = adapter
    }

    private fun subscribeWishlist(adapter : HomeAdapter) {
        viewModel.listWishlist.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun navigateToWithZSharedAxisAnimation(direction: NavDirections) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        view?.findNavController()?.navigate(direction)
    }
}