package com.dirzaaulia.gamewish.modules.details

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.rawg.GameDetails
import com.dirzaaulia.gamewish.data.models.rawg.Stores
import com.dirzaaulia.gamewish.data.models.rawg.Wishlist
import com.dirzaaulia.gamewish.databinding.FragmentDetailsBinding
import com.dirzaaulia.gamewish.modules.details.DetailsFragment.Callback
import com.dirzaaulia.gamewish.modules.details.adapter.DetailsStoresAdapter
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DetailsFragment :
    Fragment(),
    DetailsStoresAdapter.DetailsStoresAdapterListener {

    private val args: DetailsFragmentArgs by navArgs()

    private val detailsStoresAdapter = DetailsStoresAdapter(this)
//    private lateinit var detailsImageBannerAdapter: DetailsImageBannerAdapter

    @Inject
    lateinit var detailsViewModelFactory: DetailsViewModelFactory

    private val detailsViewModel: DetailsViewModel by viewModels {
        DetailsViewModel.provideFactory(detailsViewModelFactory, args.gameId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            // Scope the transition to a view in the hierarchy so we know it will be added under
            // the bottom app bar but over the elevation scale of the exiting HomeFragment.
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(ContextCompat.getColor(requireContext(), R.color.scrimBackground))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentDetailsBinding>(
            inflater,
            R.layout.fragment_details,
            container,
            false
        ).apply {
            viewModel = detailsViewModel
            lifecycleOwner = viewLifecycleOwner
            callback = Callback { gameDetails ->
                gameDetails?.let {
                    actionWishlist(it, this)
                }
            }

            setupScrollChangeListeners(this)
            subscribeGameDetails(this)
            subscribeGameDetailsScreenshots(this)
        }

        return binding.root
    }

    override fun onStoreClicked(view: View, stores: Stores) {
        val url = String.format("https://%s", stores.store?.domain)
        openLink(url)
    }

    private fun actionWishlist(
        gameDetails: GameDetails,
        binding: FragmentDetailsBinding
    ) {
        val wishlist = Wishlist(
            gameDetails.id,
            gameDetails.name,
            gameDetails.backgroundImage
        )

        val message: String

        if (detailsViewModel.itemWishlist.value == null )  {
            detailsViewModel.addToWishlist(wishlist)
            message = String.format("%s has been added to your wishlist", gameDetails.name)
            binding.detailsFab.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
            )
        } else {
            detailsViewModel.removeFromWishlist(wishlist)
            message = String.format("%s has been removed from your wishlist", gameDetails.name)
            binding.detailsFab.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_favorite_border_24
                )
            )
        }
        showSnackbarShort(binding.root, message)
    }

    private fun setupScrollChangeListeners(binding: FragmentDetailsBinding) {
        var isToolbarShown = false

        // scroll change listener begins at Y = 0 when image is fully collapsed
        binding.detailsNestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                // User scrolled past image to height of toolbar and the title text is
                // underneath the toolbar, so the toolbar should be shown.
                val shouldShowToolbar = scrollY > binding.toolbar.height


                // The new state of the toolbar differs from the previous state; update
                // appbar and toolbar attributes.
                if (isToolbarShown != shouldShowToolbar) {
                    isToolbarShown = shouldShowToolbar

                    // Use shadow animator to add elevation if toolbar is shown
                    binding.detailsAppbarLayout.isActivated = shouldShowToolbar

                    // Show the plant name if toolbar is shown
                    binding.detailsToolbarLayout.isTitleEnabled = shouldShowToolbar
                }
            }
        )
    }

    private fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }

    private fun subscribeGameDetails(binding: FragmentDetailsBinding) {
        detailsViewModel.gameDetails.observe(viewLifecycleOwner) { gameDetails ->
            detailsStoresAdapter.submitList(gameDetails.stores)
            binding.storesRecylerView.adapter = detailsStoresAdapter

            setOnClickListeners(gameDetails, binding)
        }
    }

    private fun subscribeGameDetailsScreenshots(binding: FragmentDetailsBinding) {
        detailsViewModel.gameDetailsScreenshots.observe(viewLifecycleOwner) {
            val imageList = ArrayList<SlideModel>()

            it?.forEach { screenshots ->
                imageList.add(SlideModel(screenshots.image))
            }

//            detailsImageBannerAdapter =
//                DetailsImageBannerAdapter(detailsViewModel.gameDetailsScreenshots)
//            binding.detailsImageSlider.setSliderAdapter(detailsImageBannerAdapter)
//            binding.detailsImageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM)
//            binding.detailsImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
//            binding.detailsImageSlider.scrollTimeInSec = 3
//            binding.detailsImageSlider.startAutoCycle()

            binding.detailsImageSlider.setImageList(imageList, ScaleTypes.FIT)
        }
    }

    private fun setOnClickListeners(gameDetails: GameDetails, binding: FragmentDetailsBinding) {
        //OnClickListener
        binding.detailsWebsite.setOnClickListener {
            openLink(gameDetails.website!!)
        }

        binding.detailsReddit.setOnClickListener {
            openLink(gameDetails.redditUrl!!)
        }

        binding.detailsLabelRawg.setOnClickListener {
            openLink("https://rawg.io")
        }

        //Toolbar
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }
    }

    fun interface Callback {
        fun add(gameDetails: GameDetails?)
    }


}