package com.dirzaaulia.gamewish.modules.fragment.details.anime

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.dirzaaulia.gamewish.AnimeDetailsNavGraphArgs
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.FragmentAnimeDetailsBinding
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.modules.fragment.details.anime.adapter.AnimeDetailsViewPager
import com.dirzaaulia.gamewish.modules.global.dialog.BottomSheetAnimeDialog
import com.dirzaaulia.gamewish.util.capitalizeWords
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.dirzaaulia.gamewish.util.showSnackbarShortWithAnchor
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class AnimeDetailsFragment :
    Fragment(),
    BottomSheetAnimeDialog.BottomSheetAnimeDialogListener {

    private lateinit var binding : FragmentAnimeDetailsBinding
    private lateinit var details: Details
    private lateinit var accessToken: String
    private lateinit var updateSuccessMessage : String

    private val args: AnimeDetailsNavGraphArgs by navArgs()
    private val viewModel : AnimeDetailsViewModel by hiltNavGraphViewModels(R.id.anime_details_nav_graph)

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
        binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
        subscribeAccessToken(args.id)
        subscribeUpdateResponse()
        subscribeErrorMessage()
    }

    override fun updateAnimeList(
        animeId: Int, status: String, isRewatching: Boolean?, score: Int?, episode: Int?, isUpdating: Boolean) {
        when (args.position) {
            0 -> viewModel.updateMyAnimeListAnimeList(accessToken, animeId, status, isRewatching, score, episode)
            1 -> viewModel.updateMyAnimeListAnimeList(accessToken, animeId, status, isRewatching, score, episode)
            2 -> viewModel.updateMyAnimeListMangaList(accessToken, animeId, status, isRewatching, score, episode)
        }

        if (isUpdating) {
            when (args.position) {
                0 -> updateSuccessMessage = "Your anime list has been updated"
                1 -> updateSuccessMessage = "Your anime list has been updated"
                2 -> updateSuccessMessage = "Your manga list has been updated"
            }
        } else {
            when (args.position) {
                0 -> updateSuccessMessage = "This anime has been added to your list"
                1 -> updateSuccessMessage = "This anime has been added to your list"
                2 -> updateSuccessMessage = "This manga has been added to your list"
            }
        }
    }

    override fun detailAnimeList(animeId: Int) { }

    override fun deleteAnimeList(animeId: Int) {
        when (args.position) {
            0 -> {
                viewModel.deleteMyAnimeListAnimeList(accessToken, animeId)
                updateSuccessMessage = "Anime has been deleted from your list!"
            }
            1 -> {
                viewModel.deleteMyAnimeListAnimeList(accessToken, animeId)
                updateSuccessMessage = "Anime has been deleted from your list!"
            }
            2 -> {
                viewModel.deleteMyAnimeListMangaList(accessToken, animeId)
                updateSuccessMessage = "Manga has been deleted from your list!"
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.animeDetailsFab.setOnClickListener {
            val node = Node(details.id, details.title, null, null, null)
            val listStatus = details.listStatus
            val parentNode = ParentNode(node, null, listStatus, null)

            val bottomSheet = BottomSheetAnimeDialog.newInstance(args.position, parentNode, true)
            bottomSheet.setBottomSheetAnimeDialogListener(this)
            bottomSheet.show(childFragmentManager, BottomSheetAnimeDialog::class.simpleName)
        }

        binding.animeDetailsToolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                view?.findNavController()?.navigateUp()
            }
        })
    }

    private fun subscribeAccessToken(id: Int) {
        viewModel.getSavedMyAnimeListToken()
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                startActivity(intent)
            } else {
                Timber.i("access Token : $it")
                accessToken = it

                val code = args.position
                if (code == 0 || code == 1) {
                    subscribeAnimeDetails(it, id)
                } else if (code == 2) {
                    subscribeMangaDetails(it, id)
                }
            }
        }
    }

    private fun subscribeAnimeDetails(accessToken: String, id: Int) {
        viewModel.getAnimeDetails(accessToken, id.toString())
        viewModel.itemDetails.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                setupDetails(details)
            } else {
                Timber.i("Details NULL")
            }
        }
    }

    private fun subscribeMangaDetails(accessToken: String, id: Int) {
        viewModel.getMangaDetails(accessToken, id.toString())
        viewModel.itemDetails.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                setupDetails(details)
            } else {
                Timber.i("Details NULL")
            }
        }
    }

    private fun subscribeUpdateResponse() {
        viewModel.updateResponse.observe(viewLifecycleOwner) {
            showSnackbarShortWithAnchor(binding.animeDetailsFab, binding.animeDetailsFab,  updateSuccessMessage)
            details.listStatus = it
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            showSnackbarShort(binding.root, it)
        }
    }

    private fun setupViewPager(details: Details) {
        val categories = arrayOf(
            "Description",
            "Related",
            "Recommendation"
        )

        val viewPager = binding.animeDetailsViewpager
        val tabLayout = binding.animeDetailsTabLayout

        val adapter = AnimeDetailsViewPager(childFragmentManager, lifecycle, details)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }

    private fun setupDetails(details: Details) {
        this.details = details

        if (args.position == 0 || args.position == 1) {
            binding.animeDetailsType.text = getString(R.string.anime)
            binding.animeDetailsCount.text = String.format("%d Episodes", details.episodes)
        } else {
            binding.animeDetailsType.text = getString(R.string.manga)
            binding.animeDetailsCount.text = String.format("%d Chapters", details.chapters)
        }

        var status = details.status
        if (status != null || status != "") {
            status = status?.replace("_", " ")
            status = status?.capitalizeWords()
            binding.animeDetailsStatus.text = status
        }

        val alternativeEnglishTitle = details.alternativeTitles?.en

        if (alternativeEnglishTitle == null || alternativeEnglishTitle == "") {
            binding.animeDetailsTitle.text = details.title
        } else {
            binding.animeDetailsTitle.text = alternativeEnglishTitle
        }

        val imageList = ArrayList<SlideModel>()

        details.pictures?.forEach { it ->
            imageList.add(SlideModel(it.large))
        }

        binding.animeDetailsImageSlider.setImageList(imageList, ScaleTypes.FIT)

        var media = details.mediaType
        if (media != null || media != "") {
            if (media.equals("tv", true) || media.equals("ova", true) ||
                media.equals("ona", true)) {
                media = media?.uppercase(Locale.getDefault())
            } else {
                media = media?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                media = media?.replace("_", " ")
            }

            binding.animeDetailsMedia.text = media
        }

        binding.animeDetailsScore.text = details.mean.toString()
        binding.animeDetailsRank.text = String.format("#%d", details.rank)
        binding.animeDetailsPopularity.text = String.format("#%d", details.popularity)
        binding.animeDetailsMembers.text = details.members.toString()

        setupViewPager(details)

        binding.animeDetailsProgressBar.isVisible = false
        binding.animeDetailsHeader.isVisible = true
        binding.animeDetailsFooter.isVisible = true
        binding.animeDetailsFab.isVisible = true
    }
}