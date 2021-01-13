package com.dirzaaulia.gamewish.modules.deals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.dirzaaulia.gamewish.data.models.Deals
import com.dirzaaulia.gamewish.data.models.DealsRequest
import com.dirzaaulia.gamewish.databinding.FragmentDealsBinding
import com.dirzaaulia.gamewish.modules.deals.adapter.DealsAdapter
import com.dirzaaulia.gamewish.modules.deals.adapter.DealsLoadStateAdapter
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class DealsFragment :
    Fragment(),
    DealsAdapter.DealsAdapterListener{

    private lateinit var binding: FragmentDealsBinding

    private var job: Job? = null
    private val viewModel: DealsViewModel by viewModels()
    private var adapter = DealsAdapter(this)
    private var bottomSheetBehavior = BottomSheetBehavior<ConstraintLayout>()
    private var dealsRequest = DealsRequest("1", 0, 100, "", false)
    private var storeID = "1"
    private var storeName = "Steam"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDealsBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).invalidateOptionsMenu()
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNumberFormatter()
        toggleBottomSheet()
        initAdapter()
        initOnClickListener()
        initBottomSheet()
        getStoreList()

        if (savedInstanceState != null) {
            dealsRequest = savedInstanceState.getParcelable(DEALS_FRAGMENT_DEALS_REQUEST)!!
            storeName = savedInstanceState.getString(DEALS_FRAGMENT_STORE_NAME)!!
        }
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
                toggleBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(view: View, deals: Deals) {
        openDealsId(deals.dealID)
    }

    private fun initOnClickListener() {
        binding.bottomSheetItemDeals.retryButton.setOnClickListener {
            adapter.retry()
            getStoreList()
        }

        binding.spinnerStore.setOnItemClickListener { parent, _, position, _ ->
            storeID = (position + 1).toString()
            storeName = parent.getItemAtPosition(position).toString()
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
            binding.bottomSheetItemDeals.dealsProgressBar.isVisible = loadState.source.refresh is LoadState.Loading

            // Refresh Failed
            binding.bottomSheetItemDeals.retryButton.isVisible = loadState.source.refresh is LoadState.Error
            binding.bottomSheetItemDeals.imageViewStatus.isVisible = loadState.source.refresh is LoadState.Error
            binding.bottomSheetItemDeals.textViewStatus.isVisible = loadState.source.refresh is LoadState.Error


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
        binding.buttonFilter.setOnClickListener {
            var lowerPrice: Int = Integer.parseInt(
                binding.lowerPrice.text.toString().replace(",", "")
            )
            var upperPrice: Int = Integer.parseInt(
                binding.upperPrice.text.toString().replace(",", "")
            )
            val title: String = binding.gameTitle.text.toString()
            val aaaGames: Boolean = binding.switchAAAGames.isChecked

            lowerPrice = currencyConverterLocaletoUSD(lowerPrice)
            upperPrice = currencyConverterLocaletoUSD(upperPrice)

            dealsRequest = DealsRequest(
                storeID,
                lowerPrice,
                upperPrice,
                title,
                aaaGames
            )

            refreshDeals(dealsRequest)
            toggleBottomSheet()
            viewModel.updateStoreList(storeName)
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.bottomSheetItemDeals.dealsRecyclerView.scrollToPosition(0) }
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
            binding.spinnerStore.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    listStore.map { stores ->
                        stores.storeName
                    }
                )
            )
        }
        binding.spinnerStore.setText(getString(R.string.steam), false)
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

    private fun setupView() {
        viewModel.storeName.observe(viewLifecycleOwner) {
            binding.bottomSheetItemDeals.labelStoreName.text = storeName
        }

        binding.textFieldLowerPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol
        binding.lowerPrice.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                binding.lowerPrice.text?.clear()
                setupNumberFormatter()
            }
        }

        binding.textFieldUpperPrice.prefixText = Currency.getInstance(Locale.getDefault()).symbol
        binding.upperPrice.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                binding.upperPrice.text?.clear()
            }
        }

        binding.gameTitle.setText(dealsRequest.title)

        binding.switchAAAGames.isChecked = dealsRequest.AAA!!
    }

    private fun openDealsId(dealsId: String?) {
        val url = String.format("https://www.cheapshark.com/redirect?dealID=%s", dealsId)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun setupNumberFormatter(){
        binding.lowerPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.lowerPrice.removeTextChangedListener(this)

                try {
                    var value = s.toString()

                    if (value.contains(",")) {
                        value = value.replace(",".toRegex(), "")
                    }

                    binding.lowerPrice.setText(numberFormatter(value.toDouble()))
                    binding.lowerPrice.setSelection(binding.lowerPrice.text!!.length)
                } catch (e: NumberFormatException) {
                    Timber.i(e.localizedMessage)
                }
                binding.lowerPrice.addTextChangedListener(this)
            }
        })

        binding.upperPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){}
            override fun afterTextChanged(s: Editable?) {
                binding.upperPrice.removeTextChangedListener(this)

                try {
                    var value = s.toString()

                    if (value.contains(",")) {
                        value = value.replace(",".toRegex(), "")
                    }

                    binding.upperPrice.setText(numberFormatter(value.toDouble()))
                    binding.upperPrice.setSelection(binding.upperPrice.text!!.length)
                } catch (e: NumberFormatException) {
                    Timber.i(e.localizedMessage)
                }
                binding.upperPrice.addTextChangedListener(this)
            }
        })
    }
}