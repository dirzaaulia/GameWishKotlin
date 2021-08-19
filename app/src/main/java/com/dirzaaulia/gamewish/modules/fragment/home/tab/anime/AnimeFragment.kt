package com.dirzaaulia.gamewish.modules.fragment.home.tab.anime

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.FragmentAnimeBinding
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.home.HomeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.home.HomeViewModel
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeAnimeAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.modules.global.dialog.BottomSheetAnimeDialog
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

const val ARGS_POSITION = "argsPosition"

class AnimeFragment :
    Fragment(),
    HomeAnimeAdapter.HomeAnimeAdapterListener,
    BottomSheetAnimeDialog.BottomSheetAnimeDialogListener {

    private lateinit var binding : FragmentAnimeBinding

    private var job: Job? = null
    private var position: Int = 0
    private var accessToken : String? = null
    private var sortValue : String? = null
    private var updateMessage : String? = null

    private val parentViewModel : HomeViewModel by activityViewModels()
    private val animeAdapter = HomeAnimeAdapter(this)
    private val mangaAdapter = HomeAnimeAdapter(this)

    private val openPostActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                subscribeAccessToken()
            }
        }

    companion object {
        fun newInstance(position : Int) : AnimeFragment {
            val args = Bundle()

            args.putInt(ARGS_POSITION, position)

            val fragment = AnimeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimeBinding.inflate(inflater, container, false)

        position = arguments?.getInt(ARGS_POSITION) ?: 0

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
        subscribeAccessToken()
        subscribeSortType()
        subscribeTabPosition()
        subscribeUpdateResponse()
        subscribeErrorMessage()
    }

    override fun onItemClicked(view: View, parentNode: ParentNode) {
        val bottomSheet = BottomSheetAnimeDialog.newInstance(position, parentNode, false)
        bottomSheet.setBottomSheetAnimeDialogListener(this)
        bottomSheet.show(childFragmentManager, BottomSheetAnimeDialog::class.simpleName)
    }

    override fun updateAnimeList(
        animeId: Int,
        status: String,
        isRewatching: Boolean?,
        score: Int?,
        episode: Int?,
        isUpdating: Boolean
    ) {
        updateMessage = if (isUpdating) {
            if (position == 1) {
                "Your anime list has been updated"
            } else {
                "Your manga list has been updated"
            }
        } else {
            if (position == 1) {
                "This anime has been added to your list"
            } else {
                "This manga has been added to your list"
            }
        }

        Timber.i(updateMessage)

        when (position) {
            1 -> accessToken?.let { parentViewModel.updateMyAnimeListAnimeList(it, animeId, status, isRewatching, score, episode) }
            2 -> accessToken?.let { parentViewModel.updateMyAnimeListMangaList(it, animeId, status, isRewatching, score, episode) }
        }
    }

    override fun detailAnimeList(animeId: Int) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val directions = HomeFragmentDirections.actionHomeFragmentToAnimeDetailsFragment(
            animeId,
            position
        )
        view?.findNavController()?.navigate(directions)
    }

    override fun deleteAnimeList(animeId: Int) {
        if (position == 1) {
            updateMessage = "Anime has been deleted from your list!"
            accessToken?.let { parentViewModel.deleteMyAnimeListAnimeList(it, animeId) }
        } else {
            updateMessage = "Manga has been deleted from your list!"
            accessToken?.let { parentViewModel.deleteMyAnimeListMangaList(it, animeId) }
        }
    }

    private fun setupOnClickListeners() {
        binding.retryButton.setOnClickListener {
            when (position) {
                1 -> animeAdapter.retry()
                2 -> mangaAdapter.retry()
            }
        }

        binding.animeLabel.setOnClickListener {
            openMyAnimeListLink(requireContext())
        }
    }

    private fun setupView() {
        when (position) {
            1 -> {
                subscribeAnime()
                binding.animeLabel.text = resources.getText(R.string.anime_data_provided_by_myanimelist)
            }
            2 -> {
                subscribeManga()
                binding.animeLabel.text = resources.getText(R.string.manga_data_provided_by_myanimelist)
            }
        }
    }

    private fun subscribeAccessToken() {
        parentViewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                openPostActivity.launch(intent)
            } else {
                accessToken = it
                setupView()
            }
        }
    }

    private fun subscribeSortType() {
        parentViewModel.sortType.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.animeSortType.text = String.format("Sort : All")
                sortValue = null
                when (position) {
                    1 -> refreshAnime()
                    2 -> refreshManga()
                }
            } else {
                val sort = it.replace("_", " ").capitalizeWords()
                binding.animeSortType.text = String.format("Sort : $sort")
                sortValue = it
                when (position) {
                    1 -> refreshAnime()
                    2 -> refreshManga()
                }
            }
        }
    }

    private fun subscribeTabPosition() {
        parentViewModel.tabPosition.observe(viewLifecycleOwner) {
            binding.animeSortType.text = String.format("Sort : All")
            sortValue = ""
            when (it) {
                1 -> refreshAnime()
                2 -> refreshManga()
            }
        }
    }

    private fun subscribeUpdateResponse() {
        parentViewModel.updateResponse.observe(viewLifecycleOwner) {
            updateMessage?.let { message ->
                showSnackbarShortWithAnchor(requireActivity().findViewById(R.id.bottom_nav), requireActivity()
                    .findViewById(R.id.bottom_nav), message)
            }
            when (position) {
                1 -> refreshAnime()
                2 -> refreshManga()
            }
        }
    }

    private fun subscribeErrorMessage() {
        parentViewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun subscribeAnime() {
        initAnimeAdapter()
        refreshAnime()
    }

    private fun initAnimeAdapter() {
        binding.animeRecyclerView.adapter = animeAdapter.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { animeAdapter.retry() },
            footer = GlobalGridLoadStateAdapter { animeAdapter.retry() }
        )

        animeAdapter.addLoadStateListener { loadState ->
            //Refresh Success
            binding.animeRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Show Label RAWG when data is not empty
            binding.animeLabel.isVisible = animeAdapter.itemCount >= 1
            binding.animeSortType.isVisible = animeAdapter.itemCount >= 1

            if (loadState.source.refresh is LoadState.NotLoading && animeAdapter.itemCount < 1) {
                showEmpty()
            } else if (loadState.source.refresh is LoadState.NotLoading && animeAdapter.itemCount >= 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading && animeAdapter.itemCount < 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading && animeAdapter.itemCount >= 1) {
                reloadView()
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
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
            }
        }
    }

    private fun refreshAnime() {
        job?.cancel()
        job = lifecycleScope.launch {
            accessToken?.let { accessToken ->
                if (sortValue.isNullOrEmpty()) {
                    parentViewModel.getMyAnimeListAnimeList(accessToken, null)?.collectLatest {
                        animeAdapter.submitData(it)
                    }
                } else {
                    parentViewModel.getMyAnimeListAnimeList(accessToken, sortValue)?.collectLatest {
                        animeAdapter.submitData(it)
                    }
                }
            }
        }
    }

    private fun subscribeManga() {
        initMangaAdapter()
        refreshManga()
    }

    private fun initMangaAdapter() {
        binding.animeRecyclerView.adapter = mangaAdapter.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { mangaAdapter.retry() },
            footer = GlobalGridLoadStateAdapter { mangaAdapter.retry() }
        )

        mangaAdapter.addLoadStateListener { loadState ->
            //Refresh Success
            binding.animeRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Show Label RAWG when data is not empty
            binding.animeLabel.isVisible = mangaAdapter.itemCount >= 1
            binding.animeSortType.isVisible = mangaAdapter.itemCount >= 1

            if (loadState.source.refresh is LoadState.NotLoading && mangaAdapter.itemCount < 1) {
                showEmpty()
            } else if (loadState.source.refresh is LoadState.NotLoading && mangaAdapter.itemCount >= 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading && mangaAdapter.itemCount < 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading && mangaAdapter.itemCount >= 1) {
                reloadView()
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
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
            }
        }
    }

    private fun refreshManga() {
        job?.cancel()
        job = lifecycleScope.launch {
            accessToken?.let { accessToken ->
                if (sortValue.isNullOrEmpty()) {
                    parentViewModel.getMyAnimeListMangaList(accessToken, null)?.collectLatest {
                        mangaAdapter.submitData(it)
                    }
                } else {
                    parentViewModel.getMyAnimeListMangaList(accessToken, sortValue)?.collectLatest {
                        mangaAdapter.submitData(it)
                    }
                }
            }
        }
    }

    private fun reloadView() {
        binding.animeRecyclerView.isVisible = false
        binding.retryButton.isVisible = false
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = false
        binding.progressBar.isVisible = true
    }

    private fun removeErrorView() {
        binding.animeRecyclerView.isVisible = true
        binding.retryButton.isVisible = false
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = false
    }

    private fun showNoInternet() {
        binding.animeRecyclerView.isVisible = false
        binding.retryButton.isVisible = true
        binding.imageViewStatus.isVisible = true
        binding.textViewStatus.isVisible = true
        binding.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showEmpty() {
        binding.animeRecyclerView.isVisible = false
        binding.retryButton.isVisible = false
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = true

        if (sortValue.isNullOrEmpty()) {
            when (position) {
                1 -> binding.textViewStatus.text = getString(R.string.anime_list_empty)
                2 -> binding.textViewStatus.text = getString(R.string.manga_list_empty)
            }
        } else {
            val sort = sortValue?.replace("_", " ")?.capitalizeWords()
            when (position) {
                1 -> binding.textViewStatus.text = String.format("There is no $sort anime is your list")
                2 -> binding.textViewStatus.text = String.format("There is no $sort manga is your list")
            }
        }
    }

    private fun showResponseError() {
        binding.animeRecyclerView.isVisible = false
        binding.retryButton.isVisible = true
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = true

        when (position) {
            1 -> binding.textViewStatus.text = getString(R.string.anime_empty)
            2 -> binding.textViewStatus.text = getString(R.string.manga_empty)
        }
    }
}