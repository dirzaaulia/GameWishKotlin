package com.dirzaaulia.gamewish.modules.fragment.details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.Platforms
import com.dirzaaulia.gamewish.databinding.ItemPlatformListBinding

class DetailsPlatformsAdapter : ListAdapter<Platforms, DetailsPlatformsAdapter.ViewHolder>
    (GamesDetailsPlatformsDiffCallback()) {

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
        val platforms = getItem(position)

        if (platforms != null) {
            holder.bind(platforms)
        }
    }

    class ViewHolder(
        private val binding: ItemPlatformListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(platforms: Platforms) {
            binding.apply {

                platformsItem = platforms

                when {
                    platforms.platform?.name?.contains("Xbox") == true || platforms.platform?.name?.contains("Android") == true -> {
                        mpBackgroundPlatform.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.green_700))
                    }
                    platforms.platform?.name?.contains("PlayStation") == true || platforms.platform?.name?.contains("PS") == true -> {
                        mpBackgroundPlatform.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_blue_700))
                    }
                    platforms.platform?.name?.contains("Nintendo") == true || platforms.platform?.name?.contains("Wii") == true || platforms.platform?.name?.contains("NES") == true -> {
                        mpBackgroundPlatform.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.red_700))
                    }
                }

                executePendingBindings()
            }
        }
    }
}

private class GamesDetailsPlatformsDiffCallback : DiffUtil.ItemCallback<Platforms>() {
    override fun areItemsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
        return oldItem.platform?.id == newItem.platform?.id
    }

    override fun areContentsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
        return oldItem == newItem
    }

}