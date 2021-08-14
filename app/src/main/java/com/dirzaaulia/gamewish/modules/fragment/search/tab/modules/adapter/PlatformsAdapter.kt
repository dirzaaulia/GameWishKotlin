package com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.rawg.Platform
import com.dirzaaulia.gamewish.databinding.ItemPlatformsBinding

class PlatformsAdapter(
    private val listener : PlatformsAdapterListener
) : PagingDataAdapter<Platform, PlatformsAdapter.ViewHolder>(PlatformsDiffCallback()) {

    interface PlatformsAdapterListener {
        fun onItemClicked(view : View, platform: Platform)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPlatformsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val platform = getItem(position)

        if (platform != null) {
            holder.bind(platform)
        }
    }

    class ViewHolder (
        private val binding : ItemPlatformsBinding,
        listener: PlatformsAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(platform: Platform) {
            binding.apply {
                platformItem = platform
                executePendingBindings()
            }
        }
    }
}

private class PlatformsDiffCallback : DiffUtil.ItemCallback<Platform>() {
    override fun areItemsTheSame(oldItem: Platform, newItem: Platform): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Platform, newItem: Platform): Boolean {
        return oldItem == newItem
    }
}