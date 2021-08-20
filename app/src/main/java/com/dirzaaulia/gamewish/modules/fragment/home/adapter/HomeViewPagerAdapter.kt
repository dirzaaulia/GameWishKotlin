package com.dirzaaulia.gamewish.modules.fragment.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirzaaulia.gamewish.modules.fragment.home.tab.anime.AnimeFragment
import com.dirzaaulia.gamewish.modules.fragment.home.tab.anime.MangaFragment
import com.dirzaaulia.gamewish.modules.fragment.home.tab.game.GameFragment
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.tab.SearchAnimeTabFragment

private const val NUM_TABS = 3

class HomeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return GameFragment()
            1 -> return AnimeFragment()
            2 -> return MangaFragment()
        }
        return GameFragment()
    }
}