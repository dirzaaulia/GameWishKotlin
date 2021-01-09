package com.dirzaaulia.gamewish.modules.details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.Screenshots
import com.dirzaaulia.gamewish.databinding.ItemScreenshotsListBinding

class DetailsImageAdapter(
    private val listener : DetailsImageAdapterListener
) : ListAdapter<Screenshots, DetailsImageAdapter.ViewHolder>
    (ScreenshotsDiffCallback()) {

    interface DetailsImageAdapterListener {
        fun onImageClicked(view: View, screenshots: Screenshots)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemScreenshotsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenshots = getItem(position)

        if (screenshots != null) {
            holder.bind(screenshots)
        }
    }

    class ViewHolder(
        private val binding : ItemScreenshotsListBinding,
        listener: DetailsImageAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(screenshots: Screenshots) {
            binding.apply {
                screenshotsItem = screenshots
                executePendingBindings()
            }
        }
    }
}

private class ScreenshotsDiffCallback : DiffUtil.ItemCallback<Screenshots>() {
    override fun areItemsTheSame(oldItem: Screenshots, newItem: Screenshots): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Screenshots, newItem: Screenshots): Boolean {
        return oldItem == newItem
    }

}