package com.dirzaaulia.gamewish.modules.fragment.details.anime.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.databinding.FragmentRecyclerViewBinding
import com.dirzaaulia.gamewish.modules.fragment.details.anime.adapter.AnimeDetailsAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.tab.SearchGameTabFragment
import dagger.hilt.android.AndroidEntryPoint

const val ARGS_POSITION = "argsPosition"
const val ARGS_CODE = "argsCode"
const val ARGS_DETAILS = "argsDetails"

@AndroidEntryPoint
class RecyclerViewFragment :
    Fragment(),
    AnimeDetailsAdapter.AnimeDetailsAdapterListener {

    private lateinit var binding : FragmentRecyclerViewBinding
    private val adapter = AnimeDetailsAdapter(this)
    private var position : Int = 0

    companion object {
        fun newInstance(position: Int, code :Int, details: Details) : RecyclerViewFragment {
            val args = Bundle()

            args.putInt(ARGS_POSITION, position)
            args.putInt(ARGS_CODE, code)
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

        arguments?.getParcelable<Details>(ARGS_DETAILS)?.let { initAdapter(it, arguments?.getInt(ARGS_CODE)) }

        return binding.root
    }

    override fun onItemClicked(view: View, node: Node) {

    }

    private fun initAdapter(details: Details, code: Int?) {
        binding.animeDetailsRecyclerview.adapter = adapter
        if (position == 1) {
            if (code == 1) {
                adapter.submitList(details.relatedAnime)
            } else {
                adapter.submitList(details.relatedManga)
            }
        } else {
            adapter.submitList(details.recommendations)
        }
    }
}