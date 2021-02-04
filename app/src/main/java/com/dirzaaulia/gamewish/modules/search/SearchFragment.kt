package com.dirzaaulia.gamewish.modules.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.databinding.FragmentSearchBinding
import com.dirzaaulia.gamewish.modules.search.adapter.SearchGamesAdapter
import com.dirzaaulia.gamewish.modules.search.adapter.SearchGamesLoadStateAdapter
import com.dirzaaulia.gamewish.util.SEARCH_FRAGMENT_QUERY
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), SearchGamesAdapter.SearchGamesAdapterListener {

    private lateinit var binding: FragmentSearchBinding
    private var job: Job? = null
    private val viewModel: SearchViewModel by viewModels()
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

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        initOnClickListener()
        initAdapter()
        if (savedInstanceState != null) {
            val searchQuery = savedInstanceState.getString(SEARCH_FRAGMENT_QUERY)
            refreshSearchGames(searchQuery!!)
        }
        initSearch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_FRAGMENT_QUERY, binding.searchEditText.text.trim().toString())
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

    private fun initOnClickListener() {
        binding.searchToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.searchGamesRetryButton.setOnClickListener {
            adapterSearchGames.retry()
        }

        binding.searchLabelRawg.setOnClickListener{
            openRawgLink()
        }
    }

    private fun initAdapter() {
        binding.searchRecyclerView.adapter = adapterSearchGames.withLoadStateHeaderAndFooter(
            header = SearchGamesLoadStateAdapter { adapterSearchGames.retry() },
            footer = SearchGamesLoadStateAdapter { adapterSearchGames.retry() }
        )

        adapterSearchGames.addLoadStateListener { loadState ->

            binding.searchRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            binding.searchGamesProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            binding.searchGamesRetryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.searchGamesTextViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.searchGamesImageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

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
                .collect { binding.searchRecyclerView.scrollToPosition(0) }
        }
    }

    private fun refreshSearchGames(search: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchGames(search).collectLatest {
                adapterSearchGames.submitData(it)
            }
        }
    }

    private fun updateGamesSearch() {
        binding.searchEditText.text.trim().let {
            if (it.isNotEmpty()) {
                refreshSearchGames(it.toString())
            }
        }
    }

    private fun openRawgLink() {
        val url = "https://www.rawg.io"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}