package com.dirzaaulia.gamewish.modules.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.dirzaaulia.gamewish.NavigationGraphDirections
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fab: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        fab = requireActivity().findViewById(R.id.fab)

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.homeToolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
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

        this.apply {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            fab.hide()
            fab.visibility = View.INVISIBLE
        }
        view?.findNavController()?.navigate(direction)
    }

}