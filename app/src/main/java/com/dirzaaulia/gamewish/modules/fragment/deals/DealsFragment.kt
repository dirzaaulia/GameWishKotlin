package com.dirzaaulia.gamewish.modules.fragment.deals

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.request.DealsRequest
import com.dirzaaulia.gamewish.databinding.FragmentDealsBinding
import com.dirzaaulia.gamewish.modules.fragment.deals.adapter.DealsAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class DealsFragment :
    Fragment(),
    DealsAdapter.DealsAdapterListener {

    private lateinit var binding: FragmentDealsBinding

    private var job: Job? = null
    private val viewModel: DealsViewModel by viewModels()
    private var adapter = DealsAdapter(this)
    private var storeId = 0
    private var storeName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDealsBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.dealsToolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        initOnClickListener()
        setupView()
        refreshDeals(viewModel.currentDealsRequest)
        subscribeStoreName()
        initSearchDeals()
        getStoreList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.deals_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                toggleFilterView()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(view: View, deals: Deals) {
        openDealsId(requireContext(), deals.dealID)
    }

    private fun initOnClickListener() {
        binding.dealsLayout.retryButton.setOnClickListener {
            removeErrorView()
            adapter.retry()
            getStoreList()
        }

        binding.layoutFilter.spinnerStore.setOnItemClickListener { parent, _, position, _ ->
            storeId = position + 1
            storeName = parent.getItemAtPosition(position).toString()
        }
    }

    private fun initAdapter() {
        binding.dealsLayout.dealsRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = GlobalGridLoadStateAdapter { adapter.retry() },
            footer = GlobalGridLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->
            // Refresh Success
            binding.dealsLayout.dealsRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
            binding.dealsLayout.labelStoreName.isVisible = loadState.source.refresh is LoadState.NotLoading

            // Ongoing Refresh
            binding.dealsLayout.dealsProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            if (loadState.source.refresh is LoadState.NotLoading && adapter.itemCount < 1) {
                //No Deals Found
                showNoDealsFound()
            } else if (loadState.source.refresh is LoadState.NotLoading && adapter.itemCount >= 1) {
                removeErrorView()
            } else if (loadState.source.refresh is LoadState.Loading && adapter.itemCount >= 1) {
                //Changing deals filter
                showChangingFilters()
            } else if (loadState.source.refresh is LoadState.Loading && adapter.itemCount < 1) {
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
                showSnackbarShort(binding.root,"\uD83D\uDE28 Wooops ${it.error}")
                Timber.e("loadStateAdapter : ${it.error}")
            }
        }
    }

    private fun initSearchDeals() {
        binding.layoutFilter.buttonFilter.setOnClickListener {
            val request = viewModel.currentDealsRequest
            var lowerPrice = request?.lowerPrice
            var upperPrice = request?.upperPrice
            var title: String? = binding.layoutFilter.gameTitle.text.toString()
            val aaaGames: Boolean = binding.layoutFilter.switchAAAGames.isChecked

            if (binding.layoutFilter.lowerPrice.text!!.isNotEmpty()) {
                lowerPrice = Integer.parseInt(
                    binding.layoutFilter.lowerPrice.text.toString().replace(",", "")
                )
                lowerPrice = currencyConverterLocaletoUSD(lowerPrice)
            }

            if (binding.layoutFilter.upperPrice.text!!.isNotEmpty()) {
                upperPrice = Integer.parseInt(
                    binding.layoutFilter.upperPrice.text.toString().replace(",", "")
                )
                upperPrice = currencyConverterLocaletoUSD(upperPrice)
            }

            if (title.isNullOrEmpty()) {
                title = ""
            }

            Timber.i("$storeId, $lowerPrice, $upperPrice, $title, $aaaGames")


            refreshDeals(DealsRequest(storeId.toString(), lowerPrice, upperPrice, title, aaaGames))
            viewModel.updateStoreName(storeName)
            toggleFilterView()
        }
    }

    private fun refreshDeals(request: DealsRequest) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshDeals(request)?.collect {
                adapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun getStoreList() {
        viewModel.getStoreList()
        viewModel.storeList.observe(viewLifecycleOwner) { listStore ->
            if (listStore.isNotEmpty()) {
                binding.layoutFilter.spinnerStore.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        listStore.map { stores ->
                            stores.storeName
                        }
                    )
                )
                binding.layoutFilter.spinnerStore.setText(getString(R.string.steam), false)
            } else {
                binding.layoutFilter.spinnerStore.setText(getString(R.string.store), false)
            }
        }

    }

    private fun subscribeStoreName() {
        viewModel.storeName.observe(viewLifecycleOwner) {
            binding.dealsLayout.labelStoreName.text = it
            binding.layoutFilter.spinnerStore.setText(it, false)
        }
    }

    private fun setupView() {
        val request = viewModel.currentDealsRequest

        binding.layoutFilter.textFieldLowerPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol

        binding.layoutFilter.textFieldUpperPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol

        binding.layoutFilter.gameTitle.setText(request?.title)

        binding.layoutFilter.switchAAAGames.isChecked = request?.AAA == true
    }

    private fun removeErrorView() {
        binding.dealsLayout.dealsRecyclerView.isVisible = true
        binding.dealsLayout.retryButton.isVisible = false
        binding.dealsLayout.imageViewStatus.isVisible = false
        binding.dealsLayout.textViewStatus.isVisible = false
    }

    private fun showNoInternet() {
        binding.dealsLayout.labelStoreName.isVisible = false
        binding.dealsLayout.dealsRecyclerView.isVisible = false
        binding.dealsLayout.retryButton.isVisible = true
        binding.dealsLayout.imageViewStatus.isVisible = true
        binding.dealsLayout.textViewStatus.isVisible = true
        binding.dealsLayout.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
    }

    private fun showResponseError() {
        binding.dealsLayout.labelStoreName.isVisible = false
        binding.dealsLayout.dealsRecyclerView.isVisible = false
        binding.dealsLayout.retryButton.isVisible = true
        binding.dealsLayout.imageViewStatus.isVisible = false
        binding.dealsLayout.textViewStatus.isVisible = true
        binding.dealsLayout.textViewStatus.text = getString(R.string.deals_error)
    }

    private fun showNoDealsFound() {
        binding.dealsLayout.labelStoreName.isVisible = false
        binding.dealsLayout.dealsRecyclerView.isVisible = false
        binding.dealsLayout.retryButton.isVisible = true
        binding.dealsLayout.imageViewStatus.isVisible = false
        binding.dealsLayout.textViewStatus.isVisible = true
        binding.dealsLayout.textViewStatus.text = getString(R.string.deals_not_found)
    }

    private fun showChangingFilters() {
        binding.dealsLayout.dealsRecyclerView.isVisible = false
        binding.dealsLayout.dealsProgressBar.isVisible = true
    }

    private fun toggleFilterView() {
        if (binding.dealsFilterLayout.visibility == View.GONE) {
            binding.dealsFilterLayout.visibility = View.VISIBLE
            binding.dealsLayout.dealsContainer.visibility = View.GONE
        } else {
            binding.dealsFilterLayout.visibility = View.GONE
            binding.dealsLayout.dealsContainer.visibility = View.VISIBLE
        }
    }
}