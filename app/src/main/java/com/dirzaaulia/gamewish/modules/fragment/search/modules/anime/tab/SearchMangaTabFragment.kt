package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.databinding.FragmentSearchMangaTabBinding
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeViewModel
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.openMyAnimeListLink
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchMangaTabFragment :
    Fragment(),
    SearchAnimeAdapter.SearchAnimeAdapterListener {

    private lateinit var binding : FragmentSearchMangaTabBinding

    private var job: Job? = null
    private val viewModel : SearchAnimeViewModel by hiltNavGraphViewModels(R.id.search_anime_nav_graph)
    private val adapterManga = SearchAnimeAdapter(this)
    private var query : String? = null
    private var accessToken : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchMangaTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
        initAdapterAnime()
        subscribeAccessToken()
        subscribeSearchQuery()
        subscribeErrorMessage()
    }

    override fun onItemClicked(view: View, node: Node) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val directions = SearchAnimeFragmentDirections.actionGlobalAnimeDetailsNavGraph(node.id!!, 2)
        view.findNavController().navigate(directions)
    }

    private fun setupOnClickListeners() {
        binding.searchMangaRetryButton.setOnClickListener { adapterManga.retry() }

        binding.searchLabelMyanimelist.setOnClickListener {
            openMyAnimeListLink(requireContext())
        }
    }

    private fun subscribeAccessToken() {
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                accessToken = it
            }
        }
    }

    private fun subscribeSearchQuery() {
        viewModel.searchQuery.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                accessToken?.let { accessToken -> refreshSearchManga(accessToken, it) }
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun refreshSearchManga(authorization: String, query: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchManga(authorization, query)?.collect {
                adapterManga.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun initAdapterAnime() {
        binding.searchMangaRecylerview.adapter = adapterManga.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterManga.retry() },
            footer = GlobalGridLoadStateAdapter { adapterManga.retry() }
        )

        adapterManga.addLoadStateListener { loadState ->

            //Refresh Success
            binding.searchMangaRecylerview.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchMangaProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            binding.searchLabelMyanimelist.isVisible = adapterManga.itemCount > 1

            if (loadState.source.refresh is LoadState.NotLoading && adapterManga.itemCount < 1
                && query?.isNotEmpty() == true
            ) {
                //No Search Found
                showNoItemFound()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterManga.itemCount < 1
                && query.isNullOrEmpty()) {
                //Initial Load
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterManga.itemCount > 1
                && query?.isNotEmpty() == true) {
                //Search found
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading) {
                reloadView()
            } else if (loadState.source.refresh is LoadState.Error) {
                binding.searchLabelMyanimelist.isVisible = false

                val status = (loadState.source.refresh as LoadState.Error).error.message
                if (status != null) {
                    if (status.contains("HTTP 401", false)) {
                        viewModel.refreshToken.value?.let {
                            viewModel.getMyAnimeListRefreshToken(it)
                        }
                    } else {
                        if (isOnline(requireContext())) {
                            showResponseError()
                        } else {
                            showNoInternet()
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

    private fun removeErrorView() {
        binding.searchMangaRecylerview.isVisible = true
        binding.searchMangaRetryButton.isVisible = false
        binding.searchMangaImageViewStatus.isVisible = false
        binding.searchMangaTextViewStatus.isVisible = false
    }

    private fun showNoItemFound() {
        binding.searchMangaRecylerview.isVisible = false
        binding.searchMangaRetryButton.isVisible = false
        binding.searchMangaImageViewStatus.isVisible = false
        binding.searchMangaTextViewStatus.isVisible = true
        binding.searchMangaTextViewStatus.text =
            String.format("There is no anime in this season! Try search for another season")
    }

    private fun showNoInternet() {
        binding.searchMangaRecylerview.isVisible = false
        binding.searchMangaRetryButton.isVisible = true
        binding.searchMangaImageViewStatus.isVisible = true
        binding.searchMangaTextViewStatus.isVisible = true
        binding.searchMangaTextViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.searchMangaRecylerview.isVisible = false
        binding.searchMangaRetryButton.isVisible = true
        binding.searchMangaImageViewStatus.isVisible = false
        binding.searchMangaTextViewStatus.isVisible = true
        binding.searchMangaTextViewStatus.text =
            String.format("There is no anime in this season! Try search for another season")
    }

    private fun reloadView() {
        binding.searchMangaRecylerview.isVisible = false
        binding.searchMangaRetryButton.isVisible = false
        binding.searchMangaImageViewStatus.isVisible = false
        binding.searchMangaTextViewStatus.isVisible = false
    }
}