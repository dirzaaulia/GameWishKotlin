package com.dirzaaulia.gamewish.modules.deals

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentDealsBinding
import com.dirzaaulia.gamewish.modules.deals.adapter.DealsAdapter
import com.dirzaaulia.gamewish.modules.deals.adapter.DealsLoadStateAdapter
import com.dirzaaulia.gamewish.models.DealsRequest
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DealsFragment : Fragment() {

    private lateinit var binding: FragmentDealsBinding
    private var job: Job? = null
    private val viewModel: DealsViewModel by viewModels()
    private var adapter = DealsAdapter()
    private var bottomSheetBehavior = BottomSheetBehavior<ConstraintLayout>()
    private val dealsRequest = DealsRequest("1", 0, 50, "", false)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentDealsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.dealsToolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        refreshDeals(dealsRequest)
        initAdapterRefresh()
        getStoreList()
        initBottomSheet()
        initOnClickListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.deals_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                toggleBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initOnClickListener() {
        var storeID: String? = getString(R.string.one)
        var storeName: String? = getString(R.string.steam)
        var lowerPrice: Int
        var upperPrice: Int
        var title: String
        var aaaGames: Boolean

        binding.retryButton.setOnClickListener {
            if(checkForInternet()) {
                adapter.retry()
                getStoreList()
            }
        }

        binding.spinnerStore.setOnItemClickListener { parent, _, position, _ ->
            storeID = (position + 1).toString()
            storeName = parent.getItemAtPosition(position).toString()
        }

        binding.buttonFilter.setOnClickListener {

            lowerPrice = Integer.parseInt(binding.lowerPrice.text.toString())
            upperPrice = Integer.parseInt(binding.upperPrice.text.toString())
            title = binding.gameTitle.text.toString()
            aaaGames = binding.switchAAAGames.isChecked

            val request = DealsRequest(
                storeID!!,
                lowerPrice,
                upperPrice,
                title,
                aaaGames
            )

            refreshDeals(request)
            toggleBottomSheet()
            activity?.title = getString(R.string.deals_title, storeName)
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetItemDeals.bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false
    }

    private fun initAdapter() {

        binding.bottomSheetItemDeals.dealsRecyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = DealsLoadStateAdapter { adapter.retry() },
            footer = DealsLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->

            // Refresh Success
            binding.bottomSheetItemDeals.dealsRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

            // Ongoing Refresh
            binding.dealsProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            // Refresh Failed
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Snackbar.make(binding.root, "\uD83D\uDE28 Wooops ${it.error}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshDeals(request: DealsRequest) {
        job?.cancel()
        job = lifecycleScope.launch {
            viewModel.refreshDeals(request).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun getStoreList() {
        if (checkForInternet()) {
            viewModel.getStoreList()
            viewModel.storeList.observe(viewLifecycleOwner) { listStore ->
                binding.spinnerStore.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        listStore.map { stores ->
                            stores.storeName
                        }
                    )
                )
                binding.spinnerStore.setText(listStore[0].storeName, false)
            }
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.filterLayout.visibility = View.GONE
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.filterLayout.visibility = View.VISIBLE
        }
    }

    private fun initAdapterRefresh() {
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy {
                    it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
        }
    }

    private fun checkForInternet() : Boolean {
        if (!isOnline(requireContext())) {
            showSnackbarShort(binding.root, "No internet! Please check your internet connection")
            return false
        }
        return true
    }
}