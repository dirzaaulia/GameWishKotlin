package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.ItemSearchAnimeBinding

class SearchAnimeAdapter(
    private val listener : SearchAnimeAdapterListener
): PagingDataAdapter<ParentNode, SearchAnimeAdapter.ViewHolder>(SearchAnimeDiffCallback()) {

    interface SearchAnimeAdapterListener {
        fun onItemClicked(view: View, node: Node)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchAnimeBinding.inflate(
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
        private val binding: ItemSearchAnimeBinding,
        listener: SearchAnimeAdapterListener
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

private class SearchAnimeDiffCallback : DiffUtil.ItemCallback<ParentNode>() {
    override fun areItemsTheSame(
        oldItem: ParentNode,
        newItem: ParentNode
    ): Boolean {
        return oldItem.node?.id == newItem.node?.id
    }

    override fun areContentsTheSame(
        oldItem: ParentNode,
        newItem: ParentNode
    ): Boolean {
        return oldItem.node == newItem.node
    }
}
