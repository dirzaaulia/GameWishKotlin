package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentSearchAnimeBinding
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.adapter.SearchAnimeViewPagerAdapter
import com.dirzaaulia.gamewish.util.showSnackbarShortWithAnchor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class SearchAnimeFragment : Fragment() {

    private lateinit var binding: FragmentSearchAnimeBinding

    private var accessToken : String? = null

    private val viewModel: SearchAnimeViewModel by hiltNavGraphViewModels(R.id.search_anime_nav_graph)

    private val openPostActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), this::onMyAnimeListResult)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAnimeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAccessToken()
        setupViewPager()
        setupOnClickListener()
        setupSearchView()
    }

    private fun getAccessToken() {
        val accessToken = viewModel.accessToken.value
        if (accessToken.isNullOrEmpty()) {
            Timber.i("Access Token is NULL")
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            openPostActivity.launch(intent)
        } else {
            this.accessToken = accessToken
            Timber.i("Access Token is NOT NULL")
        }
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

        binding.searchAnimeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
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

                val tabPosition =  binding.searchAnimeTabLayout.selectedTabPosition
                if (tabPosition == 0) {
                    binding.searchAnimeTabLayout.getTabAt(1)?.select()
                }
            }
        }
    }

    private fun onMyAnimeListResult(result : ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val calendar = Calendar.getInstance()

            val setYear = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            var setSeason = ""

            when (month) {
                in 0..2 -> {
                    setSeason = "winter"
                }
                in 3..5 -> {
                    setSeason = "spring"
                }
                in 6..8 -> {
                    setSeason = "summer"
                }
                in 9..11 -> {
                    setSeason = "fall"
                }
            }

            accessToken?.let { viewModel.refreshSeasonal(it, setSeason, setYear.toString()) }
        } else {
            val searchBottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.search_bottom_nav)
            searchBottomNav.selectedItemId = R.id.searchGameFragment
            showSnackbarShortWithAnchor(searchBottomNav, searchBottomNav, "You canceled MyAnimeList Account Link!")
        }
    }
}