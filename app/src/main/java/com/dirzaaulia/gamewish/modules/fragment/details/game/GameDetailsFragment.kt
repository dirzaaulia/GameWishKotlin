package com.dirzaaulia.gamewish.modules.fragment.details.game

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.data.models.rawg.GameDetails
import com.dirzaaulia.gamewish.data.models.rawg.Stores
import com.dirzaaulia.gamewish.databinding.FragmentGameDetailsBinding
import com.dirzaaulia.gamewish.modules.fragment.details.game.GameDetailsFragment.Callback
import com.dirzaaulia.gamewish.modules.fragment.details.game.adapter.DetailsStoresAdapter
import com.dirzaaulia.gamewish.util.isOnline
import com.dirzaaulia.gamewish.util.openLink
import com.dirzaaulia.gamewish.util.openRawgLink
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GameDetailsFragment :
    Fragment(),
    DetailsStoresAdapter.DetailsStoresAdapterListener {

    @Inject
    lateinit var detailsViewModelFactory: DetailsViewModelFactory

    private lateinit var userAuthId : String

    private val args: GameDetailsFragmentArgs by navArgs()
    private val detailsStoresAdapter = DetailsStoresAdapter(this)
    private val detailsViewModel: DetailsViewModel by viewModels {
        DetailsViewModel.provideFactory(detailsViewModelFactory, args.gameId)
    }

    fun interface Callback {
        fun add(gameDetails: GameDetails?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentGameDetailsBinding>(
            inflater,
            R.layout.fragment_game_details,
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

            detailsViewModel.getUserAuthId()
            userAuthId = detailsViewModel.userAuthId.value.toString()

            detailsViewModel.getLocalDataStatus()

            setupScrollChangeListeners(this)
            subscribeGameDetails(this)
            subscribeGameDetailsScreenshots(this)
            subscribeErrorMessage(this)
            setupRetryButton(this)
        }

        return binding.root
    }

    override fun onStoreClicked(view: View, stores: Stores) {
        val url = String.format("https://%s", stores.store?.domain)
        openLink(requireContext(), url)
    }

    private fun actionWishlist(
        gameDetails: GameDetails,
        binding: FragmentGameDetailsBinding
    ) {
        val wishlist = Wishlist(
            gameDetails.id,
            gameDetails.name,
            gameDetails.backgroundImage
        )
        val message: String
        val localDataStatus = detailsViewModel.localDataStatus.value

        if (detailsViewModel.wishlistItem.value == null )  {
            detailsViewModel.addToWishlist(wishlist)

            localDataStatus.let {
                if (it == true) {
                    detailsViewModel.addToWishlistFirebase(userAuthId, wishlist)
                }
            }

            message = String.format("%s has been added to your wishlist", gameDetails.name)
            binding.detailsFab.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
            )
        } else {
            detailsViewModel.removeFromWishlist(wishlist)

            localDataStatus.let {
                if (it == true) {
                    detailsViewModel.removeFromWishlistFirebase(userAuthId, wishlist)
                }
            }

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

    private fun setupScrollChangeListeners(binding: FragmentGameDetailsBinding) {
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

    private fun subscribeGameDetails(binding: FragmentGameDetailsBinding) {
        binding.detailsProgressBar.isVisible = true
        detailsViewModel.fetchGameDetails()
        detailsViewModel.gameDetails.observe(viewLifecycleOwner) { gameDetails ->
            detailsStoresAdapter.submitList(gameDetails?.stores)
            binding.storesRecylerView.adapter = detailsStoresAdapter

            setOnClickListeners(gameDetails, binding)

            showNormal(binding)
        }
    }

    private fun subscribeGameDetailsScreenshots(binding: FragmentGameDetailsBinding) {
        detailsViewModel.fetchGameDetailsScrenshoots()
        detailsViewModel.gameDetailsScreenshots.observe(viewLifecycleOwner) {
            val imageList = ArrayList<SlideModel>()

            it?.forEach { screenshots ->
                imageList.add(SlideModel(screenshots.image))
            }

            binding.detailsImageSlider.setImageList(imageList, ScaleTypes.FIT)
        }
    }

    private fun subscribeErrorMessage(binding: FragmentGameDetailsBinding) {
        detailsViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                if (isOnline(requireContext())) {
                    showConnectionError(binding)
                } else {
                    showNoInternet(binding)
                }
            }
        }
    }

    private fun setupRetryButton(binding: FragmentGameDetailsBinding) {
        binding.detailsRetryButton.setOnClickListener {
            showNormal(binding)
            binding.detailsProgressBar.isVisible = true

            subscribeGameDetails(binding)
            subscribeGameDetailsScreenshots(binding)
        }
    }

    private fun setOnClickListeners(gameDetails: GameDetails?, binding: FragmentGameDetailsBinding) {
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
            
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                  view?.findNavController()?.navigateUp()
                }
            })
        }
    }

    private fun showConnectionError(binding: FragmentGameDetailsBinding) {
        binding.detailsProgressBar.isVisible = false

        binding.detailsImageViewStatus.isVisible = false

        binding.detailsTextViewStatus.isVisible = true
        binding.detailsTextViewStatus.text = getString(R.string.game_details_empty)

        binding.detailsRetryButton.isVisible = true
    }

    private fun showNoInternet(binding: FragmentGameDetailsBinding) {
        binding.detailsProgressBar.isVisible = false

        binding.detailsImageViewStatus.isVisible = true

        binding.detailsTextViewStatus.isVisible = true
        binding.detailsTextViewStatus.text = getString(R.string.please_check_your_internet_connection)

        binding.detailsRetryButton.isVisible = true
    }

    private fun showNormal(binding : FragmentGameDetailsBinding) {
        binding.detailsImageViewStatus.isVisible = false
        binding.detailsTextViewStatus.isVisible = false
        binding.detailsRetryButton.isVisible = false
        binding.detailsProgressBar.isVisible = false
    }
}