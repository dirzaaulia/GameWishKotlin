package com.dirzaaulia.gamewish.modules.fragment.search.tab.modules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.Genre
import com.dirzaaulia.gamewish.data.models.rawg.Platform
import com.dirzaaulia.gamewish.data.models.rawg.Publisher
import com.dirzaaulia.gamewish.databinding.FragmentSearchTabBinding
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalLoadStateAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.SearchViewModel
import com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.adapter.GenresAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.adapter.PlatformsAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.adapter.PublishersAdapter
import com.dirzaaulia.gamewish.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val ARGS_POSITION = "argsPosition"

@AndroidEntryPoint
class SearchTabFragment :
    Fragment(),
    GenresAdapter.GenresAdapterListener,
    PublishersAdapter.PublishersAdapterListener,
    PlatformsAdapter.PlatformsAdapterListener{

    private lateinit var binding: FragmentSearchTabBinding
    private var job: Job? = null
    private val viewModel: SearchTabViewModel by viewModels()
    private val parentViewModel : SearchViewModel by activityViewModels()
    private val genresAdapter = GenresAdapter(this)
    private val publishersAdapter = PublishersAdapter(this)
    private val platformsAdapter = PlatformsAdapter(this)
    private var position : Int = 0

    companion object {
        fun newInstance(position : Int) : SearchTabFragment {
            val args = Bundle()
            args.putInt(ARGS_POSITION, position)

            val fragment = SearchTabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchTabBinding.inflate(inflater, container, false)

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
                1 -> getGenres()
                2 -> getPublishers()
                3 -> getPlatforms()
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

            //Error
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = genresAdapter.itemCount >= 1

            //No Genres Found
            if (loadState.source.refresh is LoadState.NotLoading && genresAdapter.itemCount < 1) {
                binding.recyclerView.isVisible = false

                if (isOnline(requireContext())) {
                    binding.textViewStatus.text = getString(R.string.genres_empty)
                } else {
                    binding.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
                }

                binding.textViewStatus.isVisible = true
                binding.retryButton.isVisible = true
            } else {
                binding.recyclerView.isVisible = true
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

            //Error
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = publishersAdapter.itemCount >= 1

            //No Publishers Found
            if (loadState.source.refresh is LoadState.NotLoading && publishersAdapter.itemCount < 1) {
                binding.recyclerView.isVisible = false

                if (isOnline(requireContext())) {
                    binding.textViewStatus.text = getString(R.string.publishers_empty)
                } else {
                    binding.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
                }

                binding.textViewStatus.isVisible = true
                binding.retryButton.isVisible = true
            } else {
                binding.recyclerView.isVisible = true
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

            //Error
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //Show Label RAWG when data is not empty
            binding.labelRawg.isVisible = platformsAdapter.itemCount >= 1

            //No Genres Found
            if (loadState.source.refresh is LoadState.NotLoading && platformsAdapter.itemCount < 1) {
                binding.recyclerView.isVisible = false

                if (isOnline(requireContext())) {
                    binding.textViewStatus.text = getString(R.string.platforms_empty)
                } else {
                    binding.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
                }

                binding.textViewStatus.isVisible = true
                binding.retryButton.isVisible = true
            } else {
                binding.recyclerView.isVisible = true
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
}