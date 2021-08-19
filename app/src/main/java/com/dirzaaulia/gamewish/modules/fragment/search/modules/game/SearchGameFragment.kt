package com.dirzaaulia.gamewish.modules.fragment.search.modules.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.Games
import com.dirzaaulia.gamewish.databinding.FragmentSearchGameBinding
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.adapter.SearchGameViewPagerAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.adapter.SearchGamesAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalLoadStateAdapter
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.openRawgLink
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchGameFragment :
    Fragment(),
    SearchGamesAdapter.SearchGamesAdapterListener {

    private lateinit var binding: FragmentSearchGameBinding
    private var job: Job? = null
    private val viewModel: SearchGameViewModel by activityViewModels()
    private var adapterSearchGames = SearchGamesAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchGameBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
        initOnClickListener()
        initAdapter()
        initSearch()
        subscribeSearchQuery()
        subscribeGenre()
        subscribePublisher()
        subscribePlatform()
    }

    override fun onGamesClicked(view: View, games: Games) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val directions = SearchGameFragmentDirections.actionSearchGameFragmentToGameDetailsFragment(games.id!!)
        view.findNavController().navigate(directions)
    }

    private fun setupViewPager() {
        val categories = arrayOf(
            "Genres",
            "Publishers",
            "Platforms",
            "Stores"
        )

        val viewPager = binding.searchGameViewpager
        val tabLayout = binding.searchGameTabLayout

        val adapter = SearchGameViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }

    private fun initOnClickListener() {
        val parentNavHost = activity?.supportFragmentManager
            ?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = parentNavHost.navController

        binding.searchGameToolbar.setNavigationOnClickListener {
            navController.navigateUp()
            viewModel.setSearchQuery("")
            viewModel.updateGenre(0)
            viewModel.updatePublisher(0)
            viewModel.updatePlatform(0)
        }

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
                    navController.navigateUp()
                    viewModel.setSearchQuery("")
                    viewModel.updateGenre(0)
                    viewModel.updatePublisher(0)
                    viewModel.updatePlatform(0)
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

            if (loadState.source.refresh is LoadState.NotLoading && adapterSearchGames.itemCount < 1) {
                showEmpty()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterSearchGames.itemCount >= 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Error) {
                if (isOnline(requireContext())) {
                    showResponseError()
                } else {
                    showNoInternet()
                }
            }

            // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Snackbar.make(binding.root, "\uD83D\uDE28 Wooops ${it.error}", Snackbar.LENGTH_SHORT).show()
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
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
    }

    private fun refreshSearchGames(search: String?, genres: Int?, publishers: Int?, platforms: Int?) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchGames(search, genres, publishers, platforms).collect {
                showBottomSheet()
                adapterSearchGames.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun updateGamesSearch() {
        binding.searchEditText.text.trim().let {
            if (it.isNotEmpty()) {
                viewModel.setSearchQuery(it.toString())
                refreshSearchGames(it.toString(), null, null, null)
            }
        }
    }

    private fun subscribeSearchQuery() {
        viewModel.searchQuery.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                refreshSearchGames(it, null, null, null)
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

    private fun removeErrorView() {
        binding.bottomSheet.searchRecyclerView.isVisible = true
        binding.bottomSheet.searchGamesRetryButton.isVisible = false
        binding.bottomSheet.searchGamesImageViewStatus.isVisible = false
        binding.bottomSheet.searchGamesTextViewStatus.isVisible = false
    }

    private fun showNoInternet() {
        binding.bottomSheet.searchRecyclerView.isVisible = false
        binding.bottomSheet.searchGamesRetryButton.isVisible = true
        binding.bottomSheet.searchGamesImageViewStatus.isVisible = true
        binding.bottomSheet.searchGamesTextViewStatus.isVisible = true
        binding.bottomSheet.searchGamesTextViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.bottomSheet.searchRecyclerView.isVisible = false
        binding.bottomSheet.searchGamesRetryButton.isVisible = true
        binding.bottomSheet.searchGamesImageViewStatus.isVisible = false
        binding.bottomSheet.searchGamesTextViewStatus.isVisible = true
        binding.bottomSheet.searchGamesTextViewStatus.text = getString(R.string.search_games_wrong)
    }

    private fun showEmpty() {
        binding.bottomSheet.searchRecyclerView.isVisible = false
        binding.bottomSheet.searchGamesRetryButton.isVisible = true
        binding.bottomSheet.searchGamesImageViewStatus.isVisible = false
        binding.bottomSheet.searchGamesTextViewStatus.isVisible = true
        binding.bottomSheet.searchGamesTextViewStatus.text = getString(R.string.search_games_empty)
    }

    private fun showBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.searchBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.searchEditText.clearFocus()
    }
}