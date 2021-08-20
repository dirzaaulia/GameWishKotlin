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
import com.dirzaaulia.gamewish.databinding.FragmentSearchSeasonalBinding
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.SearchAnimeViewModel
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.dialog.SeasonPickerDialog
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class SearchSeasonalFragment :
    Fragment(),
    SearchAnimeAdapter.SearchAnimeAdapterListener,
    SeasonPickerDialog.SeasonPickerDialogListener {

    private lateinit var binding : FragmentSearchSeasonalBinding

    private val viewModel : SearchAnimeViewModel by hiltNavGraphViewModels(R.id.search_anime_nav_graph)
    private val adapterSeasonal = SearchAnimeAdapter(this)

    private var job: Job? = null
    private var accessToken : String? = null
    private var query : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchSeasonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
        subscribeAccessToken()
        subscribeErrorMessage()
    }

    override fun onItemClicked(view: View, node: Node) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        val directions = SearchAnimeFragmentDirections.
        actionGlobalAnimeDetailsNavGraph(node.id!!, 1)

        view.findNavController().navigate(directions)
    }

    override fun updateSeasonalAnime(season: String, year: String) {
        Timber.i("Season : $season | Year : $year")
        viewModel.setSeason(String.format("${season.lowerCaseWords()} $year"))
    }

    private fun subscribeAccessToken() {
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                accessToken = it
                subscribeSeasonal()
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.searchSeasonalRetryButton.setOnClickListener {
            adapterSeasonal.retry()
        }

        binding.searchSeasonalLabelMyanimelist.setOnClickListener {
            openMyAnimeListLink(requireContext())
        }

        binding.searchSeasonalText.setOnClickListener {
            val seasonPickerDialog = SeasonPickerDialog()
            seasonPickerDialog.setSeasonPickerDialogListener(this)

            val season = viewModel.season.value
            val bundle = Bundle().apply {
                putString(SEASON_PICKER_DIALOG_KEY, season)
            }

            seasonPickerDialog.arguments = bundle
            seasonPickerDialog.show(childFragmentManager, SeasonPickerDialog::class.simpleName)
        }
    }

    private fun subscribeSeasonal() {
        initAdapterSeasonal()

        val season = viewModel.season.value
        if (season.isNullOrEmpty()) {
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

            val seasonString = String.format("$setSeason $setYear")

            viewModel.setSeason(seasonString)
        }

        viewModel.season.observe(viewLifecycleOwner) {
            refreshSeasonal()
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun refreshSeasonal() {
        val season = viewModel.season.value
        val seasonSplit = season?.split(" ")

        job?.cancel()
        job = lifecycleScope.launch {
            accessToken?.let { accessToken ->
                viewModel.refreshSeasonal(accessToken, seasonSplit?.get(1)!!, seasonSplit[0])?.collect { list ->
                    val seasonalValue = String.format("%s %s", seasonSplit[0], seasonSplit[1])
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    binding.searchSeasonalText.text = seasonalValue
                    adapterSeasonal.submitData(viewLifecycleOwner.lifecycle, list)
                }
            }
        }
    }

    private fun initAdapterSeasonal() {
        binding.searchSeasonalRecyclerview.adapter = adapterSeasonal.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapterSeasonal.retry() },
            footer = GlobalGridLoadStateAdapter { adapterSeasonal.retry() }
        )

        adapterSeasonal.addLoadStateListener { loadState ->

            //Refresh Success
            binding.searchSeasonalRecyclerview.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.searchAnimeProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            binding.searchSeasonalLabelMyanimelist.isVisible = adapterSeasonal.itemCount > 1
            binding.searchSeasonalText.isVisible = adapterSeasonal.itemCount > 1

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
                binding.searchSeasonalLabelMyanimelist.isVisible = false

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
        binding.searchSeasonalRecyclerview.isVisible = true
        binding.searchSeasonalRetryButton.isVisible = false
        binding.searchSeasonalImageViewStatus.isVisible = false
        binding.searchSeasonalTextViewStatus.isVisible = false
    }

    private fun showNoItemFound() {
        binding.searchSeasonalRecyclerview.isVisible = false
        binding.searchSeasonalRetryButton.isVisible = false
        binding.searchSeasonalImageViewStatus.isVisible = false
        binding.searchSeasonalTextViewStatus.isVisible = true
        binding.searchSeasonalTextViewStatus.text =
            String.format("There is no anime in this season! Try search for another season")
    }

    private fun showNoInternet() {
        binding.searchSeasonalRecyclerview.isVisible = false
        binding.searchSeasonalRetryButton.isVisible = true
        binding.searchSeasonalImageViewStatus.isVisible = true
        binding.searchSeasonalTextViewStatus.isVisible = true
        binding.searchSeasonalTextViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.searchSeasonalRecyclerview.isVisible = false
        binding.searchSeasonalRetryButton.isVisible = true
        binding.searchSeasonalImageViewStatus.isVisible = false
        binding.searchSeasonalTextViewStatus.isVisible = true
        binding.searchSeasonalTextViewStatus.text =
            String.format("There is no anime in this season! Try search for another season")
    }

    private fun reloadView() {
        binding.searchSeasonalRecyclerview.isVisible = false
        binding.searchSeasonalRetryButton.isVisible = false
        binding.searchSeasonalImageViewStatus.isVisible = false
        binding.searchSeasonalTextViewStatus.isVisible = false
    }
}