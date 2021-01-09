package com.dirzaaulia.gamewish.modules.details.imageviewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.Slide
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.ShortScreenshots
import com.dirzaaulia.gamewish.databinding.FragmentDetailsImageViewerBinding
import com.dirzaaulia.gamewish.util.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsImageViewerFragment : Fragment() {

    private val args : DetailsImageViewerFragmentArgs by navArgs()
    private lateinit var binding : FragmentDetailsImageViewerBinding

    private var shortScreenshotsItem : ShortScreenshots? = null

    @Inject
    lateinit var detailsImageViewerViewModelFactory : DetailsImageViewerViewModel.AssistedFactory

    private val detailsImageViewerViewModel : DetailsImageViewerViewModel by viewModels {
        DetailsImageViewerViewModel.provideFactory(
            detailsImageViewerViewModelFactory,
            args.screenshots
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate<FragmentDetailsImageViewerBinding>(
            inflater,
            R.layout.fragment_details_image_viewer,
            container,
            false
        ).apply {
            viewModel = detailsImageViewerViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            // Set transitions here so we are able to access Fragment's binding views.
            enterTransition = MaterialContainerTransform().apply {
                // Manually add the Views to be shared since this is not a standard Fragment to
                // Fragment shared element transition.
                startView = requireActivity().findViewById(R.id.short_screenshots_image_view)
                endView = sdContainer
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                scrimColor = Color.TRANSPARENT
                containerColor = requireContext().themeColor(R.attr.colorOnBackground)
                startContainerColor = requireContext().themeColor(R.attr.colorOnBackground)
                endContainerColor = requireContext().themeColor(R.attr.colorOnBackground)
            }
            returnTransition = Slide().apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
                addTarget(R.id.sd_image_view)
            }
        }
    }
}