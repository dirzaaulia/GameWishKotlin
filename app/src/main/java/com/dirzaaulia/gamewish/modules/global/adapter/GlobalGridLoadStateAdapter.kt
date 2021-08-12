package com.dirzaaulia.gamewish.modules.global.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.GridLoadStateViewItemBinding

class GlobalGridLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<GlobalGridLoadStateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder.create(parent, retry)
    }

    class ViewHolder(
        private val binding: GridLoadStateViewItemBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.dealsItemRetryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.dealsItemErrorMessage.text = loadState.error.localizedMessage
            }

            binding.dealsItemProgressBar.isVisible = loadState is LoadState.Loading
            binding.dealsItemRetryButton.isVisible = loadState is LoadState.Loading
            binding.dealsItemErrorMessage.isVisible = loadState is LoadState.Loading
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.grid_load_state_view_item, parent, false)
                val binding = GridLoadStateViewItemBinding.bind(view)

                return ViewHolder(binding, retry)
            }
        }
    }
}