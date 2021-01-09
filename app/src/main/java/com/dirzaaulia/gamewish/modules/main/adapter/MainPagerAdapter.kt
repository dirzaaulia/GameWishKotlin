package com.dirzaaulia.gamewish.modules.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirzaaulia.gamewish.modules.deals.DealsFragment
import com.dirzaaulia.gamewish.modules.home.HomeFragment
import java.lang.IndexOutOfBoundsException

const val HOME_INDEX = 0
const val DEALS_INDEX = 1

class MainPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentCreators: Map<Int, () -> Fragment> = mapOf(
        HOME_INDEX to { HomeFragment() },
        DEALS_INDEX to { DealsFragment() }
    )

    override fun getItemCount() = tabFragmentCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}