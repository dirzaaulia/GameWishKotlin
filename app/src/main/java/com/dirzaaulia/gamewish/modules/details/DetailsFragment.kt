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
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.models.Stores
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.databinding.FragmentDetailsBinding
import com.dirzaaulia.gamewish.modules.details.DetailsFragment.Callback
import com.dirzaaulia.gamewish.modules.details.adapter.DetailsImageBannerAdapter
import com.dirzaaulia.gamewish.modules.details.adapter.DetailsStoresAdapter
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.transition.MaterialContainerTransform
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DetailsFragment :
    Fragment(),
    DetailsStoresAdapter.DetailsStoresAdapterListener{

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var binding : FragmentDetailsBinding

    private val detailsStoresAdapter = DetailsStoresAdapter(this)
    private lateinit var detailsImageBannerAdapter: DetailsImageBannerAdapter

    @Inject
    lateinit var detailsViewModelFactory : DetailsViewModelFactory

    private val detailsViewModel: DetailsViewModel by viewModels {
        DetailsViewModel.provideFactory(detailsViewModelFactory, args.games)
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

        binding = DataBindingUtil.inflate<FragmentDetailsBinding>(
            inflater,
            R.layout.fragment_details,
            container,
            false
        ).apply {
            viewModel = detailsViewModel
            lifecycleOwner = viewLifecycleOwner
            callback = Callback { gameDetails ->
                gameDetails?.let {
                    val wishlist = Wishlist(
                        it.id,
                        it.name,
                        it.backgroundImage
                    )

                    if (detailsViewModel.wishlist.value?.id == null) {
                        detailsViewModel.addToWishlist(wishlist)
                        val message = String.format("%s has been added to your wishlist", it.name)
                        showSnackbarShort(root, message)
                    } else {
                        detailsViewModel.removeFromWishlist(        wishlist)
                        val message = String.format("%s has been removed from your wishlist", it.name)
                        showSnackbarShort(root, message)
                    }


                }
            }

            var isToolbarShown = false

            // scroll change listener begins at Y = 0 when image is fully collapsed
            detailsNestedScrollView.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > toolbar.height


                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar

                        // Use shadow animator to add elevation if toolbar is shown
                        detailsAppbarLayout.isActivated = shouldShowToolbar

                        // Show the plant name if toolbar is shown
                        detailsToolbarLayout.isTitleEnabled = shouldShowToolbar
                    }
                }
            )

            //Toolbar
            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }

            detailsViewModel.gameDetails.observe(viewLifecycleOwner) { gameDetails ->
                detailsStoresAdapter.submitList(gameDetails.stores)
                storesRecylerView.adapter = detailsStoresAdapter

                //OnClickListener
                detailsWebsite.setOnClickListener {
                    openLink(gameDetails.website!!)
                }

                detailsReddit.setOnClickListener {
                    openLink(gameDetails.redditUrl!!)
                }

                detailsLabelRawg.setOnClickListener {
                    openLink("https://rawg.io")
                }
            }

            detailsViewModel.gameDetailsScreenshots.observe(viewLifecycleOwner) {
                detailsImageBannerAdapter = DetailsImageBannerAdapter(detailsViewModel.gameDetailsScreenshots)
                detailsImageSlider.setSliderAdapter(detailsImageBannerAdapter)
                detailsImageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM)
                detailsImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
                detailsImageSlider.scrollTimeInSec = 3
                detailsImageSlider.startAutoCycle()
            }
        }
        
        return binding.root
    }

    override fun onStoreClicked(view: View, stores: Stores) {
        val url = String.format("https://%s", stores.store?.domain)
        openLink(url)
    }

    private fun openLink(link : String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }

    fun interface Callback {
        fun add(gameDetails: GameDetails?)
    }


}