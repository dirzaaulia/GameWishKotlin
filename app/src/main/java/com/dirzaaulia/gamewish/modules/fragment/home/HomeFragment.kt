package com.dirzaaulia.gamewish.modules.fragment.home

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeViewPagerAdapter
import com.dirzaaulia.gamewish.util.capitalizeWords
import com.dirzaaulia.gamewish.util.lowerCaseWords
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var menu : Menu

    private var sortValue : String = "all"
    private var checkedAnimeSort : Int = 0
    private var checkedMangaSort : Int = 0

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.homeToolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
        addDataFromRealtimeDatabaseToLocal()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        this.menu = menu

        val searchItem : MenuItem = menu.findItem(R.id.menu_filter_home)

        val searchView = searchItem.actionView as SearchView
        searchView.setOnCloseListener {
            searchView.onActionViewCollapsed()
            true
        }

        val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchPlate.hint = getString(R.string.game_name)

        val searchPlateView : View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchPlateView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setSearchQuery(newText) }
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                navigateToWithZSharedAxisAnimation(
                    HomeFragmentDirections.actionHomeFragmentToSearchFragment()
                )
                true
            }
            R.id.menu_sort_home -> {
                val tabPosition = viewModel.tabPosition.value

                if (tabPosition == 1) {
                    openAnimeSortDialog()
                } else {
                    openMangaSortDialog()
                }

                true
            }
            else -> false
        }
    }

    private fun navigateToWithZSharedAxisAnimation(direction: NavDirections) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        view?.findNavController()?.navigate(direction)
    }

    private fun setupViewPager() {
        val categories = arrayOf(
            "Games",
            "Anime",
            "Manga"
        )

        val viewPager = binding.homeViewPager
        val tabLayout = binding.homeTabLayout

        val adapter = HomeViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()

        binding.homeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.setTabPosition(it) }

                when (tab?.position) {
                    0 -> {
                        menu.getItem(0).isVisible = true
                        menu.getItem(2).isVisible = false
                    }
                    1 -> {
                        menu.getItem(0).isVisible = false
                        menu.getItem(2).isVisible = true
                    }
                    2 -> {
                        menu.getItem(0).isVisible = false
                        menu.getItem(2).isVisible = true
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.setTabPosition(it) }

                when (tab?.position) {
                    0 -> {
                        menu.getItem(1).isVisible = true
                        menu.getItem(2).isVisible = false
                    }
                    1 -> {
                        menu.getItem(1).isVisible = true
                        menu.getItem(2).isVisible = false
                    }
                    2 -> {
                        menu.getItem(1).isVisible = true
                        menu.getItem(2).isVisible = false
                    }
                }
            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setTabPosition(position)
            }
        })
    }

    private fun addDataFromRealtimeDatabaseToLocal() {
        auth = viewModel.getFirebaseAuth()

        viewModel.getUserAuthStatus()
        val userAuthId = viewModel.userAuthId.value

        if (userAuthId.isNullOrEmpty()) {
            Timber.i("addDataFromRealtimeDatabaseToLocal")
            viewModel.getAllWishlistFromRealtimeDatabase(auth.uid.toString())
            viewModel.setUserAuthId(auth.uid.toString())
        }
    }

    private fun openAnimeSortDialog() {
        val singleItems = arrayOf("All", "Watching", "Completed", "On Hold", "Dropped", "Plan To Watch")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sort_myanimelist))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                // Respond to neutral button press
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                // Respond to positive button press
                if (sortValue.equals("All", true)) {
                    viewModel.setSortType("")
                } else {
                    viewModel.setSortType(sortValue)
                }
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedAnimeSort) { _, which ->
                sortValue = singleItems[which]
                sortValue = sortValue.lowerCaseWords()
                sortValue = sortValue.replace(" ", "_")
                checkedAnimeSort = which
            }
            .show()
    }

    private fun openMangaSortDialog() {
        val singleItems = arrayOf("All", "Reading", "Completed", "On Hold", "Dropped", "Plan To Read")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sort_myanimelist))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                // Respond to neutral button press
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                // Respond to positive button press
                if (sortValue.equals("All", true)) {
                    viewModel.setSortType("")
                } else {
                    viewModel.setSortType(sortValue)
                }
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedMangaSort) { _, which ->
                sortValue = singleItems[which]
                sortValue = sortValue.lowerCaseWords()
                sortValue = sortValue.replace(" ", "_")
                checkedMangaSort = which
            }
            .show()
    }
}