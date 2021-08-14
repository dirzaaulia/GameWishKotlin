package com.dirzaaulia.gamewish.modules.fragment.search.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirzaaulia.gamewish.modules.fragment.search.tab.modules.SearchTabFragment

private const val NUM_TABS = 3

class SearchViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return SearchTabFragment.newInstance(position + 1)
            1 -> return SearchTabFragment.newInstance(position + 1)
            2 -> return SearchTabFragment.newInstance(position + 1)
        }
        return SearchTabFragment()
    }
}