package com.dirzaaulia.gamewish.modules.fragment.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.databinding.ItemWishlistBinding

class HomeAdapter(
    private val listener : HomeAdapterListener
) : ListAdapter<Wishlist, HomeAdapter.ViewHolder>(WishlistDiffCallback()) {

    interface HomeAdapterListener {
        fun onItemClicked(view : View, wishlist: Wishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWishlistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wishlist = getItem(position)

        if (wishlist != null) {
            holder.bind(wishlist)
        }
    }

    class ViewHolder (
        private val binding : ItemWishlistBinding,
        listener: HomeAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
           binding.run {
               this.listener = listener
           }
        }

        fun bind(wishlist: Wishlist) {
            binding.apply {
                wishlistItem = wishlist
                executePendingBindings()
            }
        }
    }
}

private class WishlistDiffCallback : DiffUtil.ItemCallback<Wishlist>() {
    override fun areItemsTheSame(oldItem: Wishlist, newItem: Wishlist): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Wishlist, newItem: Wishlist): Boolean {
        return oldItem == newItem
    }
}