package com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.rawg.Publisher
import com.dirzaaulia.gamewish.databinding.ItemPublishersBinding

class PublishersAdapter(
    private val listener : PublishersAdapterListener
) : PagingDataAdapter<Publisher, PublishersAdapter.ViewHolder>(PublishersDiffCallback()) {

    interface PublishersAdapterListener {
        fun onItemClicked(view : View, publisher: Publisher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPublishersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val publisher = getItem(position)

        if (publisher != null) {
            holder.bind(publisher)
        }
    }

    class ViewHolder (
        private val binding : ItemPublishersBinding,
        listener: PublishersAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(publisher: Publisher) {
            binding.apply {
                publisherItem = publisher
                executePendingBindings()
            }
        }
    }
}

private class PublishersDiffCallback : DiffUtil.ItemCallback<Publisher>() {
    override fun areItemsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
        return oldItem == newItem
    }
}