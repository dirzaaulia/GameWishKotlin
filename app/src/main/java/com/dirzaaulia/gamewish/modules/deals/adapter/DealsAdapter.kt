package com.dirzaaulia.gamewish.modules.deals.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.databinding.ItemGameDealsBinding
import com.dirzaaulia.gamewish.models.Deals

class DealsAdapter: PagingDataAdapter<Deals, DealsAdapter.ViewHolder>(DealsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGameDealsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deals = getItem(position)

        if (deals != null) {
            holder.bind(deals)
        }
    }

    class ViewHolder(
        private val binding: ItemGameDealsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(deals: Deals) {
            binding.apply {
                dealsItem = deals
                executePendingBindings()
            }
        }
    }
}

private class DealsDiffCallback : DiffUtil.ItemCallback<Deals>() {
    override fun areItemsTheSame(oldItem: Deals, newItem: Deals): Boolean {
        return oldItem.dealID == newItem.dealID
    }

    override fun areContentsTheSame(oldItem: Deals, newItem: Deals): Boolean {
        return oldItem == newItem
    }

}