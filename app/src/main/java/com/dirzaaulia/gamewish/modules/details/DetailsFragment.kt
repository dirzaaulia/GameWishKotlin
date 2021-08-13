package com.dirzaaulia.gamewish.modules.details

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.openLink
import com.dirzaaulia.gamewish.util.openRawgLink
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


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

    fun interface Callback {
        fun add(gameDetails: GameDetails?)
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
        openLink(requireContext(), url)
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

    private fun subscribeGameDetails(binding: FragmentDetailsBinding) {
        binding.detailsProgressBar.isVisible = true
        lifecycleScope.launch {
            delay(1000)
            detailsViewModel.fetchGameDetails()
            detailsViewModel.gameDetails.observe(viewLifecycleOwner) { gameDetails ->
                detailsStoresAdapter.submitList(gameDetails?.stores)
                binding.storesRecylerView.adapter = detailsStoresAdapter

                setOnClickListeners(gameDetails, binding)

                if (gameDetails == null) {
                    if (isOnline(requireContext())) {
                        showConnectionError(binding)
                    } else {
                        showNoInternet(binding)
                    }
                } else {
                    showNormal(binding)
                }
            }
        }
    }

    private fun subscribeGameDetailsScreenshots(binding: FragmentDetailsBinding) {
        detailsViewModel.fetchGameDetailsScrenshoots()
        detailsViewModel.gameDetailsScreenshots.observe(viewLifecycleOwner) {
            val imageList = ArrayList<SlideModel>()

            it?.forEach { screenshots ->
                imageList.add(SlideModel(screenshots.image))
            }

            binding.detailsImageSlider.setImageList(imageList, ScaleTypes.FIT)
        }
    }

    private fun setupRetryButton(binding: FragmentDetailsBinding) {
        binding.detailsRetryButton.setOnClickListener {
            showNormal(binding)
            binding.detailsProgressBar.isVisible = true

            lifecycleScope.launch {
                delay(1000)
                subscribeGameDetails(binding)
                subscribeGameDetailsScreenshots(binding)
            }
        }
    }

    private fun setOnClickListeners(gameDetails: GameDetails?, binding: FragmentDetailsBinding) {
        binding.apply {
            gameDetails?.let {
                binding.detailsWebsite.setOnClickListener {
                    openLink(requireContext(), gameDetails.website!!)
                }

                binding.detailsReddit.setOnClickListener {
                    openLink(requireContext(), gameDetails.redditUrl!!)
                }
            }

            binding.detailsLabelRawg.setOnClickListener {
                openRawgLink(requireContext())
            }

            binding.toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }
        }
    }

    private fun showConnectionError(binding: FragmentDetailsBinding) {
        binding.detailsProgressBar.isVisible = false

        binding.detailsImageViewStatus.isVisible = false

        binding.detailsTextViewStatus.isVisible = true
        binding.detailsTextViewStatus.text = getString(R.string.game_details_empty)

        binding.detailsRetryButton.isVisible = true
    }

    private fun showNoInternet(binding: FragmentDetailsBinding) {
        binding.detailsProgressBar.isVisible = false

        binding.detailsImageViewStatus.isVisible = true

        binding.detailsTextViewStatus.isVisible = true
        binding.detailsTextViewStatus.text = getString(R.string.please_check_your_internet_connection)

        binding.detailsRetryButton.isVisible = true
    }

    private fun showNormal(binding : FragmentDetailsBinding) {
        binding.detailsImageViewStatus.isVisible = false
        binding.detailsTextViewStatus.isVisible = false
        binding.detailsRetryButton.isVisible = false
        binding.detailsProgressBar.isVisible = false
    }
}