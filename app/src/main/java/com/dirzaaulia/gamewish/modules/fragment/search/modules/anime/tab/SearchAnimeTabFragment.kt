package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.tab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.databinding.FragmentSearchAnimeTabBinding
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeViewModel
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.dialog.SeasonPickerDialog
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.lowerCaseWords
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

const val ARGS_POSITION = "argsPosition"

@AndroidEntryPoint
class SearchAnimeTabFragment :
    Fragment(),
    SearchAnimeAdapter.SearchAnimeAdapterListener,
    SeasonPickerDialog.SeasonPickerDialogListener {

    private lateinit var binding : FragmentSearchAnimeTabBinding

    private var job: Job? = null
    private val viewModel : SearchAnimeTabViewModel by viewModels()
    private val parentViewModel : SearchAnimeViewModel by activityViewModels()
    private val adapterSeasonal = SearchAnimeAdapter(this)
    private val adapterAnime = SearchAnimeAdapter(this)
    private val adapterManga = SearchAnimeAdapter(this)
    private var position : Int = 0
    private var accessToken : String? = null
    private var query : String? = null

    private val openPostActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                subscribeAccessToken()
            }
        }

    companion object {
        fun newInstance(position : Int) : SearchAnimeTabFragment {
            val args = Bundle()
            args.putInt(ARGS_POSITION, position)

            val fragment = SearchAnimeTabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAnimeTabBinding.inflate(inflater, container, false)

        position = arguments?.getInt(ARGS_POSITION) ?: 0

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAccessToken()
        setupOnClickListeners()
        subscribeTabPosition()
        subscribeErrorMessage()
    }

    override fun onItemClicked(view: View, node: Node) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val directions = SearchAnimeFragmentDirections.actionSearchAnimeFragmentToAnimeDetailsFragment(
            node.id!!,
            position
        )
        view.findNavController().navigate(directions)
    }

    override fun updateSeasonalAnime(season: String, year: String) {
        accessToken?.let { refreshSeasonal(it, season, year) }
    }

    private fun subscribeAccessToken() {
        parentViewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                openPostActivity.launch(intent)
            } else {
                accessToken = it
                subscribeSearchQuery()
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.searchAnimeRetryButton.setOnClickListener {
            when (position) {
                0 -> adapterSeasonal.retry()
                1 -> adapterAnime.retry()
                2 -> adapterManga.retry()
            }
        }

        binding.searchLabelMyanimelist.setOnClickListener {
            openMyAnimeListLink(requireContext())
        }

        binding.searchAnimeSeason.setOnClickListener {
            val seasonPickerDialog = SeasonPickerDialog()
            seasonPickerDialog.setSeasonPickerDialogListener(this)
            seasonPickerDialog.show(childFragmentManager, SeasonPickerDialog::class.simpleName)
        }
    }

    private fun subscribeSearchQuery() {
        parentViewModel.searchQuery.observe(viewLifecycleOwner) {
            query = it
            if (it.isNotEmpty()) {
                Timber.i("SearchQuery is not empty")
                when (position) {
                    0 -> subscribeSeasonal()
                    1 -> subscribeAnime()
                    2 -> subscribeManga()
                }
            } else {
                Timber.i("SearchQuery is empty")
                when (position) {
                    0 -> subscribeSeasonal()
                }
            }
        }
    }

    private fun subscribeSeasonal() {
        initAdapterSeasonal()
        accessToken?.let { refreshSeasonal(it, null, null) }
    }

    private fun subscribeAnime() {
        initAdapterAnime()
        accessToken?.let { query?.let { query -> refreshSearchAnime(it, query) } }
    }

    private fun subscribeManga() {
        initAdapterManga()
        accessToken?.let { query?.let { query -> refreshSearchManga(it, query) } }
    }

    private fun subscribeTabPosition() {
        parentViewModel.tabPostion.observe(viewLifecycleOwner) {
            if (query.isNullOrEmpty()) {
                if (adapterSeasonal.itemCount < 1 || adapterAnime.itemCount < 1 || adapterManga.itemCount < 1) {
                    when (it) {
                        0 -> {
                            binding.searchAnimeTextViewStatus.isVisible = false
                        }
                        1 -> {
                            binding.searchAnimeTextViewStatus.text = getString(R.string.search_anime)
                            binding.searchAnimeTextViewStatus.isVisible = true
                        }
                        2 -> {
                            binding.searchAnimeTextViewStatus.text = getString(R.string.search_manga)
                            binding.searchAnimeTextViewStatus.isVisible = true
                        }
                    }
                }
            } else {
                when (it) {
                    0 -> accessToken?.let { token -> refreshSeasonal(token, null, null) }
                    1 -> accessToken?.let { token -> query?.let { query -> refreshSearchAnime(token, query) } }
                    2 -> accessToken?.let { token -> query?.let { query -> refreshSearchManga(token, query) } }
                }
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun refreshSeasonal(authorization: String, season: String?, year: String?) {
        if (season == null) {
            val calendar = Calendar.getInstance()

            val setYear = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            var setSeason = ""

            when (month) {
                in 0..2 -> {
                    setSeason = "winter"
                }
                in 3..5 -> {
                    setSeason = "spring"
                }
                in 6..8 -> {
                    setSeason = "summer"
                }
                in 9..11 -> {
                    setSeason = "fall"
                }
            }

            job?.cancel()
            job = lifecycleScope.launch {
                viewModel.refreshSeasonal(authorization, setYear.toString(), setSeason)?.collect { list ->
                    val seasonalValue = String.format("%s %s", setSeason, setYear.toString())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    binding.searchAnimeSeason.text = seasonalValue
                    adapterSeasonal.submitData(viewLifecycleOwner.lifecycle, list)
                }
            }
        } else {
            val seasonValue = season.lowerCaseWords()

            job?.cancel()
            job = lifecycleScope.launch {
                viewModel.refreshSeasonal(authorization, year!!, seasonValue)?.collect { list ->
                    val seasonalValue = String.format("%s %s", season, year.toString())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    binding.searchAnimeSeason.text = seasonalValue
                    adapterSeasonal.submitData(viewLifecycleOwner.lifecycle, list)
                }
            }
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

    private fun refreshSearchManga(authorization: String, query: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshSearchManga(authorization, query)?.collect {
                adapterManga.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun initAdapterSeasonal() {
        binding.searchAnimeRecylerview.adapter = adapterSeasonal.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterSeasonal.retry() },
            footer = GlobalGridLoadStateAdapter { adapterSeasonal.retry() }
        )

        adapterSeasonal.addLoadStateListener { loadState ->

            //Refresh Success
            binding.searchAnimeRecylerview.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchAnimeProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            binding.searchLabelMyanimelist.isVisible = adapterSeasonal.itemCount > 1

            if (loadState.source.refresh is LoadState.NotLoading && adapterSeasonal.itemCount < 1
                && query?.isNotEmpty() == true
            ) {
                //No Search Found
                showNoItemFound()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterSeasonal.itemCount < 1
                && query.isNullOrEmpty()) {
                //Initial Load
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapterSeasonal.itemCount > 1
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
                        parentViewModel.refreshToken.value?.let {
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

            binding.searchLabelMyanimelist.isVisible = adapterAnime.itemCount > 1

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
                binding.searchLabelMyanimelist.isVisible = false

                val status = (loadState.source.refresh as LoadState.Error).error.message
                if (status != null) {
                    if (status.contains("HTTP 401", false)) {
                        parentViewModel.refreshToken.value?.let {
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

    private fun initAdapterManga() {
        binding.searchAnimeRecylerview.adapter = adapterManga.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterManga.retry() },
            footer = GlobalGridLoadStateAdapter { adapterManga.retry() }
        )

        adapterManga.addLoadStateListener { loadState ->

            //Refresh Success
            binding.searchAnimeRecylerview.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchAnimeProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

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
                        parentViewModel.refreshToken.value?.let {
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

        when (position) {
            0 ->  binding.searchAnimeSeason.isVisible = true
            1 ->  binding.searchAnimeSeason.isVisible = false
            2 -> binding.searchAnimeSeason.isVisible = false
        }
    }

    private fun showNoItemFound() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = false
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeSeason.isVisible = false

        when (position) {
            0 -> binding.searchAnimeTextViewStatus.text =
                String.format("There is no anime in this season! Try search for another season")
            1 -> binding.searchAnimeTextViewStatus.text =
                String.format("No anime found! Please change your search query and try again")
            2 -> binding.searchAnimeTextViewStatus.text =
                String.format("No anime found! Please change your search query and try again")
        }
    }

    private fun showNoInternet() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = true
        binding.searchAnimeImageViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeTextViewStatus.text = getString(R.string.please_check_your_internet_connection)
        binding.searchAnimeSeason.isVisible = false
    }

    private fun showResponseError() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = true
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = true
        binding.searchAnimeSeason.isVisible = false

        when (position) {
            0 -> binding.searchAnimeTextViewStatus.text =
                String.format("There is no anime in this season! Try search for another season")
            1 -> binding.searchAnimeTextViewStatus.text = getString(R.string.anime_empty)
            2 -> binding.searchAnimeTextViewStatus.text = getString(R.string.manga_empty)
        }
    }

    private fun reloadView() {
        binding.searchAnimeRecylerview.isVisible = false
        binding.searchAnimeRetryButton.isVisible = false
        binding.searchAnimeImageViewStatus.isVisible = false
        binding.searchAnimeTextViewStatus.isVisible = false

        when (position) {
            0 -> binding.searchAnimeSeason.isVisible = true
            1 -> binding.searchAnimeSeason.isVisible = false
            2 -> binding.searchAnimeSeason.isVisible = false
        }
    }
}