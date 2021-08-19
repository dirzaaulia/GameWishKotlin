package com.dirzaaulia.gamewish.modules.fragment.details.anime.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.modules.fragment.details.anime.tab.DescriptionTabFragment
import com.dirzaaulia.gamewish.modules.fragment.details.anime.tab.RecyclerViewFragment
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.tab.SearchGameTabFragment

private const val NUM_TABS = 3

class AnimeDetailsViewPager(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val details: Details
)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return DescriptionTabFragment.newInstance(details)
            1 -> return RecyclerViewFragment.newInstance(position, 1, details)
            2 -> return RecyclerViewFragment.newInstance(position, 2, details)
        }
        return DescriptionTabFragment()
    }
}