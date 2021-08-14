package com.dirzaaulia.gamewish.modules.fragment.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.Games
import com.dirzaaulia.gamewish.databinding.FragmentSearchBinding
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalLoadStateAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.adapter.SearchGamesAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.adapter.SearchViewPagerAdapter
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), SearchGamesAdapter.SearchGamesAdapterListener {

    private lateinit var binding: FragmentSearchBinding
    private var job: Job? = null
    private val viewModel: SearchViewModel by activityViewModels()
    private var adapterSearchGames = SearchGamesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.doOnPreDraw { startPostponedEnterTransition() }

        setupViewPager()
        initOnClickListener()
        initAdapter()
        if (savedInstanceState != null) {
            val searchQuery = savedInstanceState.getString(SEARCH_FRAGMENT_QUERY)
            if (!searchQuery.isNullOrEmpty()) {
                refreshSearchGames(searchQuery, null, null, null)
            }
        }
        initSearch()
        subscribeGenre()
        subscribePublisher()
        subscribePlatform()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_FRAGMENT_QUERY, binding.searchEditText.text.trim().toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateGenre(0)
        viewModel.updatePublisher(0)
        viewModel.updatePlatform(0)
    }

    override fun onGamesClicked(view: View, games: Games) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val searchGamesDetailTransitionName = getString(R.string.detail_transition_name)
        val extras = FragmentNavigatorExtras(view to searchGamesDetailTransitionName)
        val directions = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(games.id!!)
        view.findNavController().navigate(directions, extras)
    }

    private fun setupViewPager() {
        val categories = arrayOf(
            "Genres",
            "Publishers",
            "Platforms",
            "Stores"
        )

        val viewPager = binding.searchViewpager
        val tabLayout = binding.searchTabLayout

        val adapter = SearchViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }

    private fun initOnClickListener() {
        binding.searchToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.bottomSheet.searchGamesRetryButton.setOnClickListener {
            adapterSearchGames.retry()
        }

        binding.bottomSheet.searchLabelRawg.setOnClickListener{
            openRawgLink(requireContext())
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.searchBottomSheet)
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }else{
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })

    }

    private fun initAdapter() {
        binding.bottomSheet.searchRecyclerView.adapter = adapterSearchGames.withLoadStateHeaderAndFooter(
            header = GlobalLoadStateAdapter { adapterSearchGames.retry() },
            footer = GlobalLoadStateAdapter { adapterSearchGames.retry() }
        )

        adapterSearchGames.addLoadStateListener { loadState ->

            //Refresh Success
            binding.bottomSheet.searchRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.bottomSheet.searchGamesProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Error
            binding.bottomSheet.searchGamesRetryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.bottomSheet.searchGamesTextViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.bottomSheet.searchGamesImageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //No Search Found
            if (loadState.source.refresh is LoadState.NotLoading && adapterSearchGames.itemCount < 1) {
                binding.bottomSheet.searchRecyclerView.isVisible = false
                binding.bottomSheet.searchGamesTextViewStatus.text = getString(R.string.search_games_not_found)
                binding.bottomSheet.searchGamesTextViewStatus.isVisible = true
            } else if (loadState.source.refresh is LoadState.Loading && adapterSearchGames.itemCount >= 1) {
                binding.bottomSheet.searchRecyclerView.isVisible = false
                binding.bottomSheet.searchLabelRawg.isVisible = false
            } else {
                binding.bottomSheet.searchRecyclerView.isVisible = true
                binding.bottomSheet.searchLabelRawg.isVisible = true
            }

            // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Snackbar.make(binding.root, "\uD83D\uDE28 Wooops ${it.error}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun initSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                updateGamesSearch()
                true
            } else {
                false
            }
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapterSearchGames.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy {
                    it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.bottomSheet.searchRecyclerView.scrollToPosition(0) }
        }
    }

    private fun refreshSearchGames(search: String?, genres: Int?, publishers: Int?, platforms: Int?) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchGames(search, genres, publishers, platforms).collect {
                showBottomSheet()
                adapterSearchGames.submitData(it)
            }
        }
    }

    private fun updateGamesSearch() {
        binding.searchEditText.text.trim().let {
            if (it.isNotEmpty()) {
                refreshSearchGames(it.toString(), null, null, null)
            }
        }
    }

    private fun subscribeGenre() {
        viewModel.genre.observe(viewLifecycleOwner) {
            if (it != 0) {
                refreshSearchGames(null, it, null, null)
            }
        }
    }

    private fun subscribePublisher() {
        viewModel.publisher.observe(viewLifecycleOwner) {
            if (it != 0){
                refreshSearchGames(null, null, it, null)
            }
        }
    }

    private fun subscribePlatform() {
        viewModel.platforms.observe(viewLifecycleOwner) {
            if (it != 0){
                refreshSearchGames(null, null, null, it)
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.searchBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.searchEditText.clearFocus()
    }
}