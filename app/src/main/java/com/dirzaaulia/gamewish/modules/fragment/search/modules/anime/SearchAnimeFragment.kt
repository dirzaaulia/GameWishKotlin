package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentSearchAnimeBinding
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeViewPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchAnimeFragment : Fragment() {

    private lateinit var binding: FragmentSearchAnimeBinding

    private val viewModel: SearchAnimeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAnimeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupOnClickListener()
        setupSearchView()
    }

    private fun setupViewPager() {
        val categories = arrayOf(
            "Seasonal Anime",
            "Anime",
            "Manga"
        )

        val viewPager = binding.searchAnimeViewPager
        val tabLayout = binding.searchAnimeTabLayout

        val adapter = SearchAnimeViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()

        viewModel.setTabPosition(0)
        binding.searchAnimeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.setTabPosition(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.setTabPosition(it) }
            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setTabPosition(position)
            }
        })
    }

    private fun setupOnClickListener() {
        val parentNavHost = activity?.supportFragmentManager
            ?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = parentNavHost.navController

        binding.searchAnimeToolbar.setOnClickListener {
            navController.navigateUp()
            viewModel.setSearchQuery("")
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigateUp()
                viewModel.setSearchQuery("")
            }
        })
    }

    private fun setupSearchView() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                updateSearch()
                true
            } else {
                false
            }
        }
    }

    private fun updateSearch() {
        binding.searchEditText.text.trim().let {
            if (it.isNotEmpty()) {
                viewModel.setSearchQuery(it.toString())

                val tabPosition =  viewModel.tabPostion.value
                if (tabPosition == 0) {
                    viewModel.setTabPosition(1)
                    binding.searchAnimeTabLayout.getTabAt(1)?.select()
                }
            }
        }
    }
}