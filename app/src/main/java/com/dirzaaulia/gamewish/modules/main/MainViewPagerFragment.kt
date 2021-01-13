package com.dirzaaulia.gamewish.modules.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentMainViewPagerBinding
import com.dirzaaulia.gamewish.modules.main.adapter.MainPagerAdapter
import com.dirzaaulia.gamewish.util.DEALS_INDEX
import com.dirzaaulia.gamewish.util.HOME_INDEX
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentMainViewPagerBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainViewPagerBinding.inflate(inflater, container, false)
        tabLayout = binding.tabs
        viewPager = binding.viewPager

        viewPager.adapter = MainPagerAdapter(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.toolbarTitle.text = getTitle(position)
            }
        })

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
            binding.toolbarTitle.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            HOME_INDEX -> R.drawable.ic_baseline_home_24
            DEALS_INDEX -> R.drawable.ic_baseline_videogame_asset_24
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            HOME_INDEX -> getString(R.string.home)
            DEALS_INDEX -> getString(R.string.deals)
            else -> null
        }
    }

    private fun getTitle(position: Int): String? {
        return when (position) {
            HOME_INDEX -> getString(R.string.app_name)
            DEALS_INDEX -> getString(R.string.deals)
            else -> null
        }
    }
}