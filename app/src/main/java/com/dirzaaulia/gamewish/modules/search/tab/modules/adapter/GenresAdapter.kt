package com.dirzaaulia.gamewish.modules.search.tab.modules.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.data.models.rawg.Genre
import com.dirzaaulia.gamewish.databinding.ItemGenresBinding

class GenresAdapter(
    private val listener : GenresAdapterListener
) : PagingDataAdapter<Genre, GenresAdapter.ViewHolder>(GenresDiffCallback()) {

    interface GenresAdapterListener {
        fun onItemClicked(view : View, genre : Genre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGenresBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = getItem(position)

        if (genre != null) {
            holder.bind(genre)
        }
    }

    class ViewHolder (
        private val binding : ItemGenresBinding,
        listener: GenresAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.run {
                this.listener = listener
            }
        }

        fun bind(genre: Genre) {
            binding.apply {
                genreItem = genre
                executePendingBindings()
            }
        }
    }
}

private class GenresDiffCallback : DiffUtil.ItemCallback<Genre>() {
    override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem == newItem
    }
}