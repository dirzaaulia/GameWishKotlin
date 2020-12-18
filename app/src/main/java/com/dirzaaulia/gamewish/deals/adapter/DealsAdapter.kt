package com.dirzaaulia.gamewish.deals.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.databinding.ItemGameDealsBinding
import com.dirzaaulia.gamewish.models.Deals

class DealsAdapter(): ListAdapter<Deals, DealsAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback: DiffUtil.ItemCallback<Deals>() {
        override fun areItemsTheSame(oldItem: Deals, newItem: Deals): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Deals, newItem: Deals): Boolean {
           return oldItem.gameID == newItem.gameID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGameDealsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val deals = getItem(position)
        holder.bind(deals)
    }

    class ViewHolder(private val binding: ItemGameDealsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(deals: Deals) {
            binding.deals = deals
            binding.executePendingBindings()
        }
    }
}