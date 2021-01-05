package com.dirzaaulia.gamewish.modules.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Platforms
import com.dirzaaulia.gamewish.databinding.ItemPlatformListBinding

class SearchGamesDetailsPlatformsAdapter : ListAdapter<Platforms, SearchGamesDetailsPlatformsAdapter.ViewHolder>
    (SearchGamesDetailsPlatformsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPlatformListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val platform = getItem(position)

        if (platform != null) {
            holder.bind(platform)
        }
    }

    class ViewHolder(
        private val binding: ItemPlatformListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(platforms: Platforms) {
            binding.apply {

                platformItem = platforms

                when {
                    platforms.platform.name?.contains("Xbox") == true || platforms.platform.name?.contains("Android") == true -> {
                        backgroundPlatform.setBackgroundColor(ContextCompat.getColor(root.context, R.color.green_700))
                    }
                    platforms.platform.name?.contains("PlayStation") == true || platforms.platform.name?.contains("PS") == true -> {
                        backgroundPlatform.setBackgroundColor(ContextCompat.getColor(root.context, R.color.light_blue_700))
                    }
                    platforms.platform.name?.contains("Nintendo") == true || platforms.platform.name?.contains("Wii") == true || platforms.platform.name?.contains("NES") == true -> {
                        backgroundPlatform.setBackgroundColor(ContextCompat.getColor(root.context, R.color.red_700))
                    }
                    else -> {
                        backgroundPlatform.setBackgroundColor(ContextCompat.getColor(root.context, R.color.color_on_surface_emphasis_high))
                    }
                }
                executePendingBindings()
            }
        }
    }
}

private class SearchGamesDetailsPlatformsDiffCallback : DiffUtil.ItemCallback<Platforms>() {
    override fun areItemsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
        return oldItem.platform.id == newItem.platform.id
    }

    override fun areContentsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
        return oldItem == newItem
    }

}