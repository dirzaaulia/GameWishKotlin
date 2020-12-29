package com.dirzaaulia.gamewish.modules.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.NavigationGraphDirections
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.dirzaaulia.gamewish.modules.main.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        activity?.findViewById<ExtendedFloatingActionButton>(R.id.fab)?.show()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.homeToolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)

//        searchView = menu.findItem(R.id.menu_search)?.actionView as SearchView
//        setupSearchView(searchView)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                navigateToWithZSharedAxisAnimation(
                    NavigationGraphDirections.actionGlobalSearchFragment()
                )
                true
            }
            else -> false
        }
    }

    private fun navigateTo(direction: NavDirections) {
        view?.findNavController()?.navigate(direction)
    }

    private fun navigateToWithZSharedAxisAnimation(direction: NavDirections) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

//        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.animate()?.setListener(object : AnimatorListenerAdapter() {
//            var isCanceled = false
//            override fun onAnimationEnd(animation: Animator?) {
//                if (isCanceled) return
//
//                // Hide the BottomAppBar to avoid it showing above the keyboard
//                // when composing a new email.
//                activity?.findViewById<ExtendedFloatingActionButton>(R.id.fab)?.hide()
//                activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.GONE
//            }
//            override fun onAnimationCancel(animation: Animator?) {
//                isCanceled = true
//            }
//        })

        view?.findNavController()?.navigate(direction)
    }
//    private fun setupSearchView(searchView: SearchView) {
//        searchView.setOnCloseListener { true }
//        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return true
//            }
//        })
//    }
}