package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.databinding.FragmentSearchAnimeBinding
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalLoadStateAdapter
import com.dirzaaulia.gamewish.util.SEARCH_ANIME_TAB_SELECTED
import com.dirzaaulia.gamewish.util.SEARCH_FRAGMENT_QUERY
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchAnimeFragment :
    Fragment(),
    SearchAnimeAdapter.SearchAnimeAdapterListener {

    private lateinit var binding: FragmentSearchAnimeBinding
    private val viewModel : SearchAnimeViewModel by viewModels()
    private var job: Job? = null
    private var adapterSearchAnime = SearchAnimeAdapter(this)
    private var accessToken : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAnimeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListener()
        setupTabLayout()
        initAdapter()
        checkSaveInstanceState(savedInstanceState)
        setupSearchView()
        subscribeErrorMessage()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SEARCH_ANIME_TAB_SELECTED, binding.searchAnimeTabLayout.selectedTabPosition)
        outState.putString(SEARCH_FRAGMENT_QUERY, binding.searchEditText.text.trim().toString())
    }

    override fun onItemClicked(view: View, node: Node) {

    }

    private fun setupOnClickListener() {
        val parentNavHost = activity?.supportFragmentManager
            ?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = parentNavHost.navController

        binding.searchAnimeToolbar.setOnClickListener {
            navController.navigateUp()
        }

        binding.searchAnimeRetryButton.setOnClickListener {
            adapterSearchAnime.retry()
        }
    }

    private fun checkSaveInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val searchQuery = savedInstanceState.getString(SEARCH_FRAGMENT_QUERY)
            val tabPosition = savedInstanceState.getInt(SEARCH_ANIME_TAB_SELECTED)

            binding.searchAnimeTabLayout.getTabAt(tabPosition)?.isSelected

            if (searchQuery != null) {
                subscribeAccessToken(searchQuery, tabPosition)
            }
        }
    }

    private fun subscribeAccessToken(query: String, tabPosition : Int) {
        viewModel.getSavedMyAnimeListToken()
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                startActivity(intent)
            } else {
                when (tabPosition) {
                    0 -> {
                        refreshSearchAnime(it, query)
                    }
                    1 -> {
                        refreshSearchManga(it, query)
                    }
                    else -> {
                        refreshSearchAnime(it, query)
                    }
                }
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showSnackbarShort(binding.root, it)
            }
        }
    }

    private fun updateSearch(tabPosition: Int) {
        binding.searchEditText.text.trim().let {
            if (it.isNotEmpty()) {
                subscribeAccessToken(it.toString(), tabPosition)
            }
        }
    }

    private fun refreshSearchAnime(authorization: String, query: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchAnime(authorization, query)?.collect {
                adapterSearchAnime.submitData(it)
            }
        }
    }

    private fun refreshSearchManga(authorization: String, query: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchManga(authorization, query)?.collect {
                adapterSearchAnime.submitData(it)
            }
        }
    }

    private fun initAdapter() {
        binding.searchAnimeRecylerview.adapter = adapterSearchAnime.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterSearchAnime.retry() },
            footer = GlobalGridLoadStateAdapter { adapterSearchAnime.retry() }
        )

        adapterSearchAnime.addLoadStateListener { loadState ->

            val query = binding.searchEditText.text.toString()

            //Refresh Success
            binding.searchAnimeRecylerview.isVisible = loadState.source.refresh is LoadState.NotLoading
            binding.searchAnimeTabLayout.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchAnimeProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Error
            binding.searchAnimeRetryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.searchAnimeTextViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.searchAnimeImageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //No Search Found
            if (loadState.source.refresh is LoadState.NotLoading && adapterSearchAnime.itemCount < 1
                && query.isNotEmpty()
            ) {
                binding.searchAnimeRecylerview.isVisible = false
                binding.searchAnimeTabLayout.isVisible = false
                binding.searchAnimeTextViewStatus.text = getString(R.string.search_anime_not_found)
                binding.searchAnimeTextViewStatus.isVisible = true
            } else if (loadState.source.refresh is LoadState.NotLoading
                && adapterSearchAnime.itemCount < 1 && query.isEmpty()
            ) {
                binding.searchAnimeRecylerview.isVisible = false
                binding.searchAnimeTabLayout.isVisible = false
                binding.searchLabelMyanimelist.isVisible = false
            } else  if (loadState.source.refresh is LoadState.Error) {
                Timber.i((loadState.source.refresh as LoadState.Error).error.localizedMessage)

                binding.searchLabelMyanimelist.isVisible = false

                val status = (loadState.source.refresh as LoadState.Error).error.message
                if (status != null) {
                    if (status.contains("HTTP 401", false)) {
                        viewModel.refreshToken.value?.let {
                            viewModel.getMyAnimeListRefreshToken(it)
                        }
                    }
                }
            }

            // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Timber.i(it.error)
                Snackbar.make(binding.root, "\uD83D\uDE28 Wooops ${it.error}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                updateSearch(0)
                true
            } else {
                false
            }
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapterSearchAnime.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy {
                    it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.searchAnimeRecylerview.scrollToPosition(0) }
        }
    }

    private fun setupTabLayout() {
        binding.searchAnimeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position!!
                updateSearch(tabPosition)

                if (tabPosition == 0) {
                    binding.searchLabelMyanimelist.text = getString(R.string.anime_data_provided_by_myanimelist)
                } else {
                    binding.searchLabelMyanimelist.text = getString(R.string.manga_data_provided_by_myanimelist)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position!!
                updateSearch(tabPosition)

                if (tabPosition == 0) {
                    binding.searchLabelMyanimelist.text = getString(R.string.anime_data_provided_by_myanimelist)
                } else {
                    binding.searchLabelMyanimelist.text = getString(R.string.manga_data_provided_by_myanimelist)
                }
            }
        })
    }
}