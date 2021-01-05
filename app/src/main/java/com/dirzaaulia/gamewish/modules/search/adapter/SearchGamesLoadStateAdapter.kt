package com.dirzaaulia.gamewish.modules.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ItemSearchGamesLoadStateViewItemBinding

class SearchGamesLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<SearchGamesLoadStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
       holder.bind(loadState)
    }

    class ViewHolder(
                    private val binding: ItemSearchGamesLoadStateViewItemBinding,
            retry: () -> Unit
            ) : RecyclerView.ViewHolder(binding.root) {

                init {
                    binding.searchGamesItemRetryButton.setOnClickListener { retry.invoke() }
                }

                fun bind(loadState: LoadState) {
                    if (loadState is LoadState.Error) {
                binding.searchGamesItemErrorMessage.text = loadState.error.localizedMessage
            }

            binding.searchGamesItemProgressBar.isVisible = loadState is LoadState.Loading
            binding.searchGamesItemRetryButton.isVisible = loadState is LoadState.Loading
            binding.searchGamesItemErrorMessage.isVisible = loadState is LoadState.Loading
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_games_load_state_view_item, parent, false)

                val binding = ItemSearchGamesLoadStateViewItemBinding.bind(view)

                return ViewHolder(binding, retry)
            }
        }
    }
}