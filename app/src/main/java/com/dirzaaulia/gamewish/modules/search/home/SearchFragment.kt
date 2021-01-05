package com.dirzaaulia.gamewish.modules.search.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentSearchBinding
import com.dirzaaulia.gamewish.modules.search.adapter.SearchGamesAdapter
import com.dirzaaulia.gamewish.modules.search.adapter.SearchGamesLoadStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var job: Job? = null
    private val viewModel: SearchViewModel by viewModels()
    private var adapterSearchGames = SearchGamesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchLabelRawg.isVisible = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            initAdapter()
            initOnClickListener()
            initAdapterRefresh()
        }
    }

    private fun initOnClickListener() {
        binding.searchToolbar.run {
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        binding.searchGamesRetryButton.run {
            adapterSearchGames.retry()
        }

        binding.searchEditText.run {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    refreshSearchGames(binding.searchEditText.text.toString())
                }
                false
            }
        }

//        binding.searchLabelRawg.run {
//            setOnClickListener {
//                openRawgLink()
//            }
//        }
    }

    private fun initAdapter() {
        binding.searchRecyclerView.run {
             adapter = adapterSearchGames.withLoadStateHeaderAndFooter(
                header = SearchGamesLoadStateAdapter { adapterSearchGames.retry() },
                footer = SearchGamesLoadStateAdapter { adapterSearchGames.retry() }
            )

            adapterSearchGames.addLoadStateListener { loadState ->

                binding.searchLabelRawg.isVisible = loadState.source.refresh is LoadState.NotLoading
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
    }

    private fun refreshSearchGames(search: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchGames(search).collectLatest {
                adapterSearchGames.submitData(it)
            }
        }
    }

    private fun initAdapterRefresh() {
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapterSearchGames.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy {
                    it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
        }
    }

    private fun openRawgLink() {
        val url = "https://www.rawg.io"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}