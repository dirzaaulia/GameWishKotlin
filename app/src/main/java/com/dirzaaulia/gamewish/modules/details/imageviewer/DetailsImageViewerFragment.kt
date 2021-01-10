package com.dirzaaulia.gamewish.modules.details.imageviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentDetailsImageViewerBinding
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsImageViewerFragment : Fragment() {

    private val args : DetailsImageViewerFragmentArgs by navArgs()
    private lateinit var binding : FragmentDetailsImageViewerBinding

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
            enterTransition = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }
    }
}