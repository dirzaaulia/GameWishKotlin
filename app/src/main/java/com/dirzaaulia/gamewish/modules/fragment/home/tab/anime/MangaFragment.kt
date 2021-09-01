package com.dirzaaulia.gamewish.modules.fragment.home.tab.anime

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.HomeTabNavGraphDirections
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.FragmentMangaBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.fragment.home.HomeFragmentDirections
import com.dirzaaulia.gamewish.modules.fragment.home.HomeViewModel
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeAnimeAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.modules.global.dialog.BottomSheetAnimeDialog
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MangaFragment :
    Fragment(),
    HomeAnimeAdapter.HomeAnimeAdapterListener,
    BottomSheetAnimeDialog.BottomSheetAnimeDialogListener {

    private lateinit var binding : FragmentMangaBinding

    private var job: Job? = null
    private var accessToken : String? = null
    private var updateMessage : String? = null
    private var sortValue : String = ""
    private var checkedMangaSort : Int = 0

    private val viewModel : HomeViewModel by hiltNavGraphViewModels(R.id.home_tab_nav_graph)
    private val mangaAdapter = HomeAnimeAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMangaBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(requireActivity().findViewById(R.id.home_toolbar))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
        subscribeAccessToken()
        subscribeUpdateResponse()
        subscribeErrorMessage()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        menu.getItem(0).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                navigateToWithZSharedAxisAnimation(
                    HomeFragmentDirections.actionAnimeFragmentToSearchFragment(R.id.search_anime_nav_graph)
                )
                true
            }
            R.id.menu_sort -> {
                openMangaSortDialog()
                true
            }
            else -> false
        }
    }

    private fun navigateToWithZSharedAxisAnimation(direction: NavDirections) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        view?.findNavController()?.navigate(direction)
    }

    override fun onItemClicked(view: View, parentNode: ParentNode) {
        val bottomSheet = BottomSheetAnimeDialog.newInstance(2, parentNode, false)
        bottomSheet.setBottomSheetAnimeDialogListener(this)
        bottomSheet.show(childFragmentManager, BottomSheetAnimeDialog::class.simpleName)
    }

    override fun updateAnimeList(
        animeId: Int, status: String, isRewatching: Boolean?,
        score: Int?, episode: Int?, isUpdating: Boolean) {
        updateMessage = if (isUpdating) {
            "Your manga list has been updated"
        } else {
            "This manga has been added to your list"
        }

        accessToken?.let { viewModel.updateMyAnimeListMangaList(it, animeId, status, isRewatching, score, episode) }
    }

    override fun detailAnimeList(animeId: Int) {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        val directions = HomeTabNavGraphDirections.actionGlobalAnimeDetailsNavGraph(animeId, 2)
        view?.findNavController()?.navigate(directions)
    }

    override fun deleteAnimeList(animeId: Int) {
        updateMessage = "Manga has been deleted from your list!"
        accessToken?.let { viewModel.deleteMyAnimeListMangaList(it, animeId) }
    }

    private fun setupOnClickListeners() {
        binding.retryButton.setOnClickListener { mangaAdapter.retry() }

        binding.animeLabel.setOnClickListener { openMyAnimeListLink(requireContext()) }
    }

    private fun subscribeAccessToken() {
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                accessToken = it
                subscribeManga()
            }
        }
    }

    private fun subscribeUpdateResponse() {
        viewModel.updateResponse.observe(viewLifecycleOwner) {
            updateMessage?.let { message ->
                showSnackbarShortWithAnchor(requireActivity().findViewById(R.id.bottom_nav), requireActivity()
                    .findViewById(R.id.bottom_nav), message)
            }
            refreshManga()
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
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
                if (sortValue.isEmpty()) {
                    viewModel.getMyAnimeListMangaList(accessToken, null)?.collectLatest {
                        mangaAdapter.submitData(it)
                    }
                } else {
                    viewModel.getMyAnimeListMangaList(accessToken, sortValue)?.collectLatest {
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

        if (sortValue.isEmpty()) {
            binding.textViewStatus.text = getString(R.string.manga_list_empty)
        } else {
            val sort = sortValue.replace("_", " ").capitalizeWords()
            binding.textViewStatus.text = String.format("There is no $sort manga is your list")
        }
    }

    private fun showResponseError() {
        binding.animeRecyclerView.isVisible = false
        binding.retryButton.isVisible = true
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = true
        binding.textViewStatus.text = getString(R.string.manga_empty)
    }

    private fun openMangaSortDialog() {
        val singleItems = arrayOf("All", "Reading", "Completed", "On Hold", "Dropped", "Plan To Read")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sort_myanimelist))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                // Respond to neutral button press
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                // Respond to positive button press
                if (sortValue.equals("All", true)) {
                    sortValue = ""
                } else {
                    sortValue
                }
                refreshManga()
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedMangaSort) { _, which ->
                sortValue = singleItems[which]
                sortValue = sortValue.lowerCaseWords()
                sortValue = sortValue.replace(" ", "_")
                checkedMangaSort = which
            }
            .show()
    }

}