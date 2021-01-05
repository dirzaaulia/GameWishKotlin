package com.dirzaaulia.gamewish.modules.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.databinding.ItemSearchGamesBinding
import com.dirzaaulia.gamewish.modules.search.home.SearchFragmentDirections

class SearchGamesAdapter: PagingDataAdapter<Games, SearchGamesAdapter.ViewHolder>(
    SearchGamesDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchGamesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchGames = getItem(position)

        if (searchGames != null) {
            holder.bind(searchGames)
        }
    }

    class ViewHolder(
        private val binding: ItemSearchGamesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.gamesItem?.let { games ->
                    navigateToSearchDetails(games, it)
                }
            }
        }
        
        private fun navigateToSearchDetails(games: Games, view: View) {
            val directions = SearchFragmentDirections.actionSearchFragmentToSearchDetailsFragment(games)
            view.findNavController().navigate(directions)
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