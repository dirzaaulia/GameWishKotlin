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
import com.dirzaaulia.gamewish.databinding.FragmentSearchAnimeTabBinding
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
import java.util.*

@AndroidEntryPoint
class SearchAnimeTabFragment :
    Fragment(),
    SearchAnimeAdapter.SearchAnimeAdapterListener {

    private lateinit var binding : FragmentSearchAnimeTabBinding

    private var job: Job? = null
    private val viewModel : SearchAnimeViewModel by hiltNavGraphViewModels(R.id.search_anime_nav_graph)
    private val adapterAnime = SearchAnimeAdapter(this)
    private var query : String? = null
    private var accessToken : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAnimeTabBinding.inflate(inflater, container, false)
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

        val directions = SearchAnimeFragmentDirections.actionGlobalAnimeDetailsNavGraph(node.id!!, 1)
        view.findNavController().navigate(directions)
    }

    private fun setupOnClickListeners() {
        binding.searchAnimeRetryButton.setOnClickListener { adapterAnime.retry() }

        binding.searchAnimeLabelMyanimelist.setOnClickListener {
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
                accessToken?.let { accessToken -> refreshSearchAnime(accessToken, it) }
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun refreshSearchAnime(authorization: String, query: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchAnime(authorization, query)?.collect {
                adapterAnime.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun initAdapterAnime() {
        binding.searchAnimeRecylerview.adapter = adapterAnime.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterAnime.retry() },
            footer = GlobalGridLoadStateAdapter { adapterAnime.retry() }
        )

        adapterAnime.addLoadStateListener { loadState ->

            //Refresh Success
            binding.searchAnimeRecylerview.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchAnimeProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            binding.searchAnimeLabelMyanimelist.isVisible = adapterAnime.itemCount > 1

            if (loadState.source.refresh is LoadState.NotLoading && adapterAnime.itemCount < 1
                && query?.isNotEmpty() == true
            ) {
                //No Search Found
                showNoItemFound()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterAnime.itemCount < 1
                && query.isNullOrEmpty()) {
                //Initial Load
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterAnime.itemCount > 1
                && query?.isNotEmpty() == true) {
                //Search found
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading) {
                reloadView()
            } else if (loadState.source.refresh is LoadState.Error) {
                binding.searchAnimeLabelMyanimelist.isVisible = false

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
        binding.searchAnimeRecylerview.isVisible = true
        binding.searchAnimeRetryButton.isVisible = false
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = false
    }

    private fun showNoItemFound() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = false
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.text =
                String.format("There is no anime in this season! Try search for another season")
    }

    private fun showNoInternet() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = true
        binding.searchAnimeImageViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = true
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.text =
                String.format("There is no anime in this season! Try search for another season")
    }

    private fun reloadView() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = false
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = false
    }
}