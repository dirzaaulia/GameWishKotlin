package com.dirzaaulia.gamewish.modules.fragment.home.tab.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.databinding.FragmentGameBinding
import com.dirzaaulia.gamewish.modules.fragment.home.HomeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.home.HomeViewModel
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeAdapter
import com.google.android.material.transition.MaterialFadeThrough
import timber.log.Timber

class GameFragment :
    Fragment(),
    HomeAdapter.HomeAdapterListener {

    private lateinit var binding : FragmentGameBinding

    private val parentViewModel : HomeViewModel by activityViewModels()
    private val adapter = HomeAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeWishlist()
    }

    override fun onItemClicked(view: View, wishlist: Wishlist) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(wishlist.id!!)
        view.findNavController().navigate(directions)
    }

    private fun subscribeWishlist() {
        binding.homeRecyclerView.adapter = adapter
        parentViewModel.listWishlist.observe(viewLifecycleOwner) {
            binding.homeEmptyLabel.isVisible = it.isEmpty()
            adapter.submitList(it)
        }
    }
}