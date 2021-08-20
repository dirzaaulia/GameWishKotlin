package com.dirzaaulia.gamewish.modules.fragment.details.anime.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.databinding.FragmentRecyclerViewBinding
import com.dirzaaulia.gamewish.modules.fragment.details.anime.AnimeDetailsViewModel
import com.dirzaaulia.gamewish.modules.fragment.details.anime.adapter.AnimeDetailsAdapter
import dagger.hilt.android.AndroidEntryPoint

const val ARGS_POSITION = "argsPosition"
const val ARGS_DETAILS = "argsDetails"

@AndroidEntryPoint
class RecyclerViewFragment :
    Fragment(),
    AnimeDetailsAdapter.AnimeDetailsAdapterListener {

    private lateinit var binding : FragmentRecyclerViewBinding

    private val viewModel : AnimeDetailsViewModel by hiltNavGraphViewModels(R.id.anime_details_nav_graph)
    private val adapter = AnimeDetailsAdapter(this)
    private var position : Int = 0
    private var isAnime : Boolean = false

    companion object {
        fun newInstance(position: Int, details: Details) : RecyclerViewFragment {
            val args = Bundle()

            args.putInt(ARGS_POSITION, position)
            args.putParcelable(ARGS_DETAILS, details)

            val fragment = RecyclerViewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)

        position = arguments?.getInt(ARGS_POSITION) ?: 0

        arguments?.getParcelable<Details>(ARGS_DETAILS)?.let { initAdapter(it) }

        return binding.root
    }

    override fun onItemClicked(view: View, node: Node) {
        val accessToken = viewModel.accessToken.value
        if (position == 1 ) {
            if (isAnime) {
                accessToken?.let { viewModel.getAnimeDetails(it, node.id.toString()) }
            } else {
                accessToken?.let { viewModel.getAnimeDetails(it, node.id.toString())
            }
        }
        }
    }

    private fun initAdapter(details: Details) {
        binding.animeDetailsRecyclerview.adapter = adapter
        if (position == 1) {
            val relatedAnime = details.relatedAnime
            val relatedManga = details.relatedManga


            if (!relatedAnime.isNullOrEmpty()) {
                isAnime = true
                adapter.submitList(relatedAnime)
            } else {
                isAnime = false
                adapter.submitList(relatedManga)
            }
        } else {
            adapter.submitList(details.recommendations)
        }
    }
}