package com.dirzaaulia.gamewish.modules.deals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.request.DealsRequest
import com.dirzaaulia.gamewish.databinding.FragmentDealsBinding
import com.dirzaaulia.gamewish.modules.deals.adapter.DealsAdapter
import com.dirzaaulia.gamewish.modules.global.adapter.GlobalGridLoadStateAdapter
import com.dirzaaulia.gamewish.modules.main.MainActivity
import com.dirzaaulia.gamewish.util.DEALS_FRAGMENT_DEALS_REQUEST
import com.dirzaaulia.gamewish.util.DEALS_FRAGMENT_STORE_NAME
import com.dirzaaulia.gamewish.util.currencyConverterLocaletoUSD
import com.dirzaaulia.gamewish.util.isOnline
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class DealsFragment :
    Fragment(),
    DealsAdapter.DealsAdapterListener{

    private lateinit var binding: FragmentDealsBinding

    private var job: Job? = null
    private val viewModel: DealsViewModel by viewModels()
    private var adapter = DealsAdapter(this)
    private var dealsRequest = DealsRequest("1", 0, 100, "", false)
    private var storeID = "1"
    private var storeName = "Steam"

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
        getStoreList()
        checkSavedInstanceState(savedInstanceState)
        setupView()
        refreshDeals(dealsRequest)
        initSearchDeals()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(DEALS_FRAGMENT_DEALS_REQUEST, dealsRequest)
        outState.putString(DEALS_FRAGMENT_STORE_NAME, storeName)
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
        openDealsId(deals.dealID)
    }

    private fun checkSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            dealsRequest = savedInstanceState.getParcelable(DEALS_FRAGMENT_DEALS_REQUEST)!!
            storeName = savedInstanceState.getString(DEALS_FRAGMENT_STORE_NAME)!!
        }
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

    private fun initOnClickListener() {
        binding.dealsLayout.retryButton.setOnClickListener {
            adapter.retry()
            getStoreList()
        }

        binding.layoutFilter.spinnerStore.setOnItemClickListener { parent, _, position, _ ->
            storeID = (position + 1).toString()
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

            // Ongoing Refresh
            binding.dealsLayout.dealsProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            // Refresh Failed
            binding.dealsLayout.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.dealsLayout.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.dealsLayout.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error

            //No Deals Found
            if (loadState.source.refresh is LoadState.NotLoading && adapter.itemCount < 1) {
                binding.dealsLayout.dealsRecyclerView.isVisible = false

                if (isOnline(requireContext())) {
                    binding.dealsLayout.textViewStatus.text = getString(R.string.deals_not_found)
                } else {
                    binding.dealsLayout.textViewStatus.text = getString(R.string.please_check_your_internet_connection)
                }

                binding.dealsLayout.textViewStatus.isVisible = true
            } else {
                binding.dealsLayout.dealsRecyclerView.isVisible = true
            }


            // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Snackbar.make(
                    binding.root,
                    "\uD83D\uDE28 Wooops ${it.error}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initSearchDeals() {
        binding.layoutFilter.buttonFilter.setOnClickListener {
            var lowerPrice = dealsRequest.lowerPrice
            var upperPrice = dealsRequest.upperPrice
            val title: String = binding.layoutFilter.gameTitle.text.toString()
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

            dealsRequest = DealsRequest(storeID, lowerPrice, upperPrice, title, aaaGames)

            refreshDeals(dealsRequest)

            toggleFilterView()
            viewModel.updateStoreList(storeName)
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.dealsLayout.dealsRecyclerView.scrollToPosition(0) }
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
            }
        }
        binding.layoutFilter.spinnerStore.setText(getString(R.string.steam), false)
    }

    private fun setupView() {
        viewModel.storeName.observe(viewLifecycleOwner) {
            binding.dealsLayout.labelStoreName.text = storeName
        }

        binding.layoutFilter.textFieldLowerPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol

        binding.layoutFilter.textFieldUpperPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol

        binding.layoutFilter.gameTitle.setText(dealsRequest.title)

        binding.layoutFilter.switchAAAGames.isChecked = dealsRequest.AAA!!
    }

    private fun openDealsId(dealsId: String?) {
        val url = String.format("https://www.cheapshark.com/redirect?dealID=%s", dealsId)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}