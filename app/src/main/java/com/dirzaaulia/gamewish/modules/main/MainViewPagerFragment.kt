package com.dirzaaulia.gamewish.modules.main

import android.os.Bundle
import android.view.*
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentMainViewPagerBinding
import com.dirzaaulia.gamewish.modules.main.adapter.DEALS_INDEX
import com.dirzaaulia.gamewish.modules.main.adapter.HOME_INDEX
import com.dirzaaulia.gamewish.modules.main.adapter.MainPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMainViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = MainPagerAdapter(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.toolbarTitle.text = getAppBarTitle(position)
            }
        })

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
            binding.toolbarTitle.text = getAppBarTitle(position)
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

    private fun getAppBarTitle(position: Int): String? {
        return when (position) {
            HOME_INDEX -> getString(R.string.app_name)
            DEALS_INDEX -> getString(R.string.deals_on_steam)
            else -> null
        }
    }}