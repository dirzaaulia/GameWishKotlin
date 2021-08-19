package com.dirzaaulia.gamewish.modules.fragment.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.ItemAnimeListBinding
import com.dirzaaulia.gamewish.databinding.ItemWishlistBinding
import com.dirzaaulia.gamewish.util.capitalizeWords

class HomeAnimeAdapter(
    private val listener : HomeAnimeAdapterListener
) : PagingDataAdapter<ParentNode, HomeAnimeAdapter.ViewHolder>(HomeAnimeDiffCallback()) {

    interface HomeAnimeAdapterListener {
        fun onItemClicked(view : View, parentNode: ParentNode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAnimeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parentNode = getItem(position)

        if (parentNode != null) {
            holder.bind(parentNode)
        }
    }

    class ViewHolder (
        private val binding : ItemAnimeListBinding,
        listener: HomeAnimeAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(parentNode: ParentNode) {
            binding.apply {
                nodeItem = parentNode

                val score = parentNode.listStatus?.score
                itemAnimeRating.text = String.format("Your Rating : $score")

                var status = parentNode.listStatus?.status
                status = status?.replace("_", " ")
                status = status?.capitalizeWords()
                itemAnimeStatus.text = status

                val isRewatching = parentNode.listStatus?.isRewatching
                val isReReading = parentNode.listStatus?.isReReading

                if (isRewatching == true) {
                    itemAnimeRewatching.text = String.format("Rewatching")
                } else if (isReReading == true) {
                    itemAnimeRewatching.text = String.format("Rereading")
                }

                val episodes = parentNode.listStatus?.episodes
                val chapters = parentNode.listStatus?.chapters

                if (status.equals("plan to watch", true) || status.equals("plan to read", true)) {
                    itemAnimeEpisode.isVisible = false
                } else if (episodes != null) {
                    itemAnimeEpisode.text = String.format("$episodes Episodes Watched")
                } else if (chapters != null) {
                    itemAnimeEpisode.text = String.format("$chapters Chapters Read")
                }

                executePendingBindings()
            }
        }
    }
}

private class HomeAnimeDiffCallback : DiffUtil.ItemCallback<ParentNode>() {
    override fun areItemsTheSame(oldItem: ParentNode, newItem: ParentNode): Boolean {
        return oldItem.node?.id == newItem.node?.id
    }

    override fun areContentsTheSame(oldItem: ParentNode, newItem: ParentNode): Boolean {
        return oldItem == newItem
    }
}