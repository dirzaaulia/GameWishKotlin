package com.dirzaaulia.gamewish.modules.fragment.home.tab.game

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.databinding.FragmentGameBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.fragment.home.HomeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.home.HomeViewModel
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeAdapter
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis

class GameFragment :
    Fragment(),
    HomeAdapter.HomeAdapterListener {

    private lateinit var binding : FragmentGameBinding

    private val viewModel : HomeViewModel by hiltNavGraphViewModels(R.id.home_tab_nav_graph)
    private val adapter = HomeAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(requireActivity().findViewById(R.id.home_toolbar))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeWishlist()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        menu.getItem(1).isVisible = false
        val searchItem : MenuItem = menu.findItem(R.id.menu_filter_home)

        val searchView = searchItem.actionView as SearchView
        searchView.setOnCloseListener {
            searchView.onActionViewCollapsed()
            true
        }

        val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchPlate.hint = getString(R.string.game_name)

        val searchPlateView : View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchPlateView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setSearchQuery(newText) }
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                navigateToWithZSharedAxisAnimation(
                    HomeFragmentDirections.actionGameFragmentToSearchFragment(R.id.searchGameFragment)
                )
                true
            }
            else -> false
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

    override fun onItemClicked(view: View, wishlist: Wishlist) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val directions = HomeFragmentDirections.actionGameFragmentToGameDetailsFragment(wishlist.id!!)
        view.findNavController().navigate(directions)
    }

    private fun subscribeWishlist() {
        binding.homeRecyclerView.adapter = adapter
        viewModel.listWishlist.observe(viewLifecycleOwner) {
            binding.homeEmptyLabel.isVisible = it.isEmpty()
            adapter.submitList(it)
        }
    }
}