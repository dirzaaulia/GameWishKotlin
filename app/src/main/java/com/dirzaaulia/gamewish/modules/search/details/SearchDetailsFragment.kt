package com.dirzaaulia.gamewish.modules.search.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentSearchDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchDetailsFragment : Fragment() {

    private val args: SearchDetailsFragmentArgs by navArgs()

    @Inject
    lateinit var searchDetailsViewModelFactory : SearchDetailsViewModel.AssistedFactory

    private val searchDetailsViewModel: SearchDetailsViewModel by viewModels {
        SearchDetailsViewModel.provideFactory(
            searchDetailsViewModelFactory,
            args.games
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentSearchDetailsBinding>(
            inflater,
            R.layout.fragment_search_details,
            container,
            false
        ).apply {
            viewModel = searchDetailsViewModel
            lifecycleOwner = viewLifecycleOwner

            var isToolbarShown = false

            // scroll change listener begins at Y = 0 when image is fully collapsed
            searchDetailsNestedScrollView.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > toolbar.height

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar

                        // Use shadow animator to add elevation if toolbar is shown
                        searchDetailsAppbarLayout.isActivated = shouldShowToolbar

                        // Show the plant name if toolbar is shown
                        searchDetailsToolbarLayout.isTitleEnabled = shouldShowToolbar
                    }
                }
            )

            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}