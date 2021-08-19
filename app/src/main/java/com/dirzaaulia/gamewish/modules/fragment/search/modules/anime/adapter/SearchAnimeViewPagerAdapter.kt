package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.tab.SearchAnimeTabFragment
import com.dirzaaulia.gamewish.modules.fragment.search.modules.game.tab.SearchGameTabFragment

private const val NUM_TABS = 3

class SearchAnimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return SearchAnimeTabFragment.newInstance(position)
            1 -> return SearchAnimeTabFragment.newInstance(position)
            2 -> return SearchAnimeTabFragment.newInstance(position)
        }
        return SearchAnimeTabFragment()
    }
}