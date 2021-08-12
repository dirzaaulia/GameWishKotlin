package com.dirzaaulia.gamewish.modules.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.rawg.Games
import com.dirzaaulia.gamewish.databinding.ItemSearchGamesBinding

class SearchGamesAdapter(
    private val listener : SearchGamesAdapterListener
): PagingDataAdapter<Games, SearchGamesAdapter.ViewHolder>(SearchGamesDiffCallback()) {

    interface SearchGamesAdapterListener {
        fun onGamesClicked(view: View, games: Games)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchGamesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchGames = getItem(position)

        if (searchGames != null) {
            holder.bind(searchGames)
        }
    }

    class ViewHolder(
        private val binding: ItemSearchGamesBinding,
        listener: SearchGamesAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(games: Games) {
            binding.apply {
                gamesItem = games
                executePendingBindings()
            }
        }
    }
}

private class SearchGamesDiffCallback : DiffUtil.ItemCallback<Games>() {
    override fun areItemsTheSame(
        oldItem: Games,
        newItem: Games
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Games,
        newItem: Games
    ): Boolean {
        return oldItem == newItem
    }
}