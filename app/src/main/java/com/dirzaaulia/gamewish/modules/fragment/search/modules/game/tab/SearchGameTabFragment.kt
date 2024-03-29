package com.dirzaaulia.gamewish.modules.fragment.search.modules.game.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.Genre
import com.dirzaaulia.gamewish.data.models.rawg.Platform
import com.dirzaaulia.gamewish.data.models.rawg.Publisher
import com.dirzaaulia.gamewish.databinding.FragmentSearchGameTabBinding
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.SearchGameViewModel
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.adapter.GenresAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.adapter.PlatformsAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.adapter.PublishersAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalLoadStateAdapter
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.openRawgLink
import com.dirzaaulia.gamewish.util.showSnackbarShort
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val ARGS_POSITION = "argsPosition"

@AndroidEntryPoint
class SearchGameTabFragment :
    Fragment(),
    GenresAdapter.GenresAdapterListener,
    PublishersAdapter.PublishersAdapterListener,
    PlatformsAdapter.PlatformsAdapterListener{

    private lateinit var binding: FragmentSearchGameTabBinding

    private var job: Job? = null
    private val viewModel: SearchGameTabViewModel by viewModels()
    private val parentViewModel : SearchGameViewModel by hiltNavGraphViewModels(R.id.search_game_nav_graph)
    private val genresAdapter = GenresAdapter(this)
    private val publishersAdapter = PublishersAdapter(this)
    private val platformsAdapter = PlatformsAdapter(this)
    private var position : Int = 0

    companion object {
        fun newInstance(position : Int) : SearchGameTabFragment {
            val args = Bundle()
            args.putInt(ARGS_POSITION, position)

            val fragment = SearchGameTabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchGameTabBinding.inflate(inflater, container, false)

        position = arguments?.getInt(ARGS_POSITION) ?: 0

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewBasedOnTab()
        setupOnClickListeners()
    }

    //onItemClicked Genre
    override fun onItemClicked(view: View, genre: Genre) {
        genre.id?.let { parentViewModel.updateGenre(it) }
    }

    //onItemClicked Publisher
    override fun onItemClicked(view: View, publisher: Publisher) {
        publisher.id?.let { parentViewModel.updatePublisher(it) }
    }

    //onItemClicked Platforms
    override fun onItemClicked(view: View, platform: Platform) {
        platform.id?.let { parentViewModel.updatePlatform(it) }
    }

    private fun setupViewBasedOnTab() {
        when (position) {
            1 -> {
                subscribeGenres()
                binding.labelRawg.text = resources.getText(R.string.genres_data_provided_by_rawg)
            }
            2 -> {
                subscribePublishers()
                binding.labelRawg.text = resources.getText(R.string.publishers_data_provided_by_rawg)
            }
            3 -> {
                subscribePlatforms()
                binding.labelRawg.text = resources.getText(R.string.platforms_data_provided_by_rawg)
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.retryButton.setOnClickListener {
            when (position) {
                1 -> genresAdapter.retry()
                2 -> publishersAdapter.retry()
                3 -> platformsAdapter.retry()
            }
        }

        binding.labelRawg.setOnClickListener {
            openRawgLink(requireContext())
        }
    }

    private fun subscribeGenres() {
        initGenresAdapter()
        getGenres()
    }

    private fun initGenresAdapter() {
        binding.recyclerView.adapter = genresAdapter.withLoadStateHeaderAndFooter(
            header = GlobalLoadStateAdapter { genresAdapter.retry() },
            footer = GlobalLoadStateAdapter { genresAdapter.retry() }
        )

        genresAdapter.addLoadStateListener { loadState ->
            //Refresh Success
            binding.recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = genresAdapter.itemCount >= 1

            if (loadState.source.refresh is LoadState.NotLoading && genresAdapter.itemCount < 1) {
                showResponseError()
            } else if (loadState.source.refresh is LoadState.NotLoading && genresAdapter.itemCount >= 1) {
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
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
            }
        }
    }

    private fun getGenres() {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.getGenres().collectLatest {
                genresAdapter.submitData(it)
            }
        }
    }

    private fun subscribePublishers() {
        initPublishersAdapter()
        getPublishers()
    }

    private fun initPublishersAdapter() {
        binding.recyclerView.adapter = publishersAdapter.withLoadStateHeaderAndFooter(
            header = GlobalLoadStateAdapter { publishersAdapter.retry() },
            footer = GlobalLoadStateAdapter { publishersAdapter.retry() }
        )

        publishersAdapter.addLoadStateListener { loadState ->
            //Refresh Success
            binding.recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = publishersAdapter.itemCount >= 1

            if (loadState.source.refresh is LoadState.NotLoading && publishersAdapter.itemCount < 1) {
                showResponseError()
            } else if (loadState.source.refresh is LoadState.NotLoading && publishersAdapter.itemCount >= 1) {
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
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
            }
        }
    }

    private fun getPublishers() {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.getPublishers().collectLatest {
                publishersAdapter.submitData(it)
            }
        }
    }

    private fun subscribePlatforms() {
        initPlatformsAdapter()
        getPlatforms()
    }

    private fun initPlatformsAdapter() {
        binding.recyclerView.adapter = platformsAdapter.withLoadStateHeaderAndFooter(
            header = GlobalLoadStateAdapter { platformsAdapter.retry() },
            footer = GlobalLoadStateAdapter { platformsAdapter.retry() }
        )

        platformsAdapter.addLoadStateListener { loadState ->
            //Refresh Success
            binding.recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            //Loading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = platformsAdapter.itemCount >= 1

            if (loadState.source.refresh is LoadState.NotLoading && platformsAdapter.itemCount < 1) {
                showResponseError()
            } else if (loadState.source.refresh is LoadState.NotLoading && platformsAdapter.itemCount >= 1) {
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
                showSnackbarShort(binding.root, "\uD83D\uDE28 Wooops ${it.error}")
            }
        }
    }

    private fun getPlatforms() {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.getPlatforms().collectLatest {
                platformsAdapter.submitData(it)
            }
        }
    }

    private fun removeErrorView() {
        binding.recyclerView.isVisible = true
        binding.retryButton.isVisible = false
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = false
    }

    private fun showNoInternet() {
        binding.recyclerView.isVisible = false
        binding.retryButton.isVisible = true
        binding.imageViewStatus.isVisible = true
        binding.textViewStatus.isVisible = true
        binding.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.recyclerView.isVisible = false
        binding.retryButton.isVisible = true
        binding.imageViewStatus.isVisible = false
        binding.textViewStatus.isVisible = true

        when (position) {
            1 -> binding.textViewStatus.text = getString(R.string.genres_empty)
            2 -> binding.textViewStatus.text = getString(R.string.publishers_empty)
            3 -> binding.textViewStatus.text = getString(R.string.platforms_empty)
        }
    }
}