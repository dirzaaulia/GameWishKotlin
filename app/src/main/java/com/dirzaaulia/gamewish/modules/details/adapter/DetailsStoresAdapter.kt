package com.dirzaaulia.gamewish.modules.details.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.Stores
import androidx.recyclerview.widget.ListAdapter
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ItemStoresListBinding

class DetailsStoresAdapter(
    private val listener : DetailsStoresAdapterListener
) : ListAdapter<Stores, DetailsStoresAdapter.ViewHolder>(GamesStoresDiffCallback()) {

    interface DetailsStoresAdapterListener {
        fun onStoreClicked(view : View, stores: Stores)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStoresListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stores = getItem(position)

        if (stores != null) {
            holder.bind(stores)
        }
    }

    class ViewHolder (
        private val binding : ItemStoresListBinding,
        listener: DetailsStoresAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(stores: Stores) {
            binding.apply {
                storesItem = stores

                when {
                    stores.store?.name?.contains("Xbox") == true || stores.store?.name?.contains("Android") == true -> {
                        storesBackground.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.green_700))
                    }
                    stores.store?.name?.contains("PlayStation") == true || stores.store?.name?.contains("PS") == true -> {
                        storesBackground.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.light_blue_700))
                    }
                    stores.store?.name?.contains("Nintendo") == true || stores.store?.name?.contains("Wii") == true || stores.store?.name?.contains("NES") == true -> {
                        storesBackground.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.red_700))
                    }
                }
                
                executePendingBindings()
            }
        }

    }
}

private class GamesStoresDiffCallback : DiffUtil.ItemCallback<Stores>() {
    override fun areItemsTheSame(oldItem: Stores, newItem: Stores): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Stores, newItem: Stores): Boolean {
        return oldItem == newItem
    }

}