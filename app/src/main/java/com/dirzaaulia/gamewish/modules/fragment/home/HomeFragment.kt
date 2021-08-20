package com.dirzaaulia.gamewish.modules.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.viewpager2.widget.ViewPager2
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.home.adapter.HomeViewPagerAdapter
import com.dirzaaulia.gamewish.util.capitalizeWords
import com.dirzaaulia.gamewish.util.lowerCaseWords
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.dirzaaulia.gamewish.util.showSnackbarShortWithAnchor
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

    private var tabPosition : Int = 0

    private val viewModel: HomeViewModel by hiltNavGraphViewModels(R.id.home_tab_nav_graph)

    private val openPostActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), this::onMyAnimeListResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
        addDataFromRealtimeDatabaseToLocal()
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
                tabPosition = tab?.position!!
                if (tabPosition == 1 || tabPosition == 2) {
                    val accessToken = viewModel.accessToken.value
                    if (accessToken.isNullOrEmpty()) {
                        val intent = Intent(requireContext(), WebViewActivity::class.java)
                        openPostActivity.launch(intent)
                    } else {
                        binding.homeTabLayout.getTabAt(tabPosition)?.select()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { }

            override fun onTabReselected(tab: TabLayout.Tab?) { }

        })
    }

    private fun addDataFromRealtimeDatabaseToLocal() {
        auth = viewModel.getFirebaseAuth()
        val userAuthId = viewModel.userAuthId.value
        if (userAuthId.isNullOrEmpty()) {
            Timber.i("addDataFromRealtimeDatabaseToLocal")
            viewModel.getAllWishlistFromRealtimeDatabase(auth.uid.toString())
            viewModel.setUserAuthId(auth.uid.toString())
        }
    }

    private fun onMyAnimeListResult(result : ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            binding.homeTabLayout.getTabAt(tabPosition)?.select()
        } else {
            binding.homeTabLayout.getTabAt(0)?.select()
            showSnackbarShortWithAnchor(requireActivity().findViewById(R.id.bottom_nav),
                requireActivity().findViewById(R.id.bottom_nav), "You canceled MyAnimeList Account Link!")
        }
    }
}