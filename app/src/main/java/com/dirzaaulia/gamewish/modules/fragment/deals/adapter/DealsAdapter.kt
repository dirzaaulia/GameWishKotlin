package com.dirzaaulia.gamewish.modules.fragment.deals.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.databinding.ItemGameDealsBinding

class DealsAdapter(
  private val listener : DealsAdapterListener
) : PagingDataAdapter<Deals, DealsAdapter.ViewHolder>(DealsDiffCallback()) {

    interface DealsAdapterListener {
        fun onItemClicked(view : View, deals: Deals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGameDealsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deals = getItem(position)

        if (deals != null) {
            holder.bind(deals)
        }
    }

    class ViewHolder(
        private val binding: ItemGameDealsBinding,
        listener: DealsAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

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