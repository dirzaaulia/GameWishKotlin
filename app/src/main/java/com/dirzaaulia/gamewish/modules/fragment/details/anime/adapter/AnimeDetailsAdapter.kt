package com.dirzaaulia.gamewish.modules.fragment.details.anime.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.ItemAnimeDetailsBinding

class AnimeDetailsAdapter (
    private val listener : AnimeDetailsAdapterListener
): ListAdapter<ParentNode, AnimeDetailsAdapter.ViewHolder>(AnimeDetailsDiffCallback()) {

    interface AnimeDetailsAdapterListener {
        fun onItemClicked(view: View, node: Node)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAnimeDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val node = getItem(position)

        if (node != null) {
            holder.bind(node)
        }
    }

    class ViewHolder(
        private val binding: ItemAnimeDetailsBinding,
        listener: AnimeDetailsAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(node: ParentNode) {
            binding.apply {
                nodeItem = node.node
                executePendingBindings()
            }
        }
    }
}

private class AnimeDetailsDiffCallback : DiffUtil.ItemCallback<ParentNode>() {
    override fun areItemsTheSame(oldItem: ParentNode, newItem: ParentNode): Boolean {
        return oldItem.node?.id == newItem.node?.id
    }

    override fun areContentsTheSame(oldItem: ParentNode, newItem: ParentNode): Boolean {
        return oldItem == newItem
    }

}