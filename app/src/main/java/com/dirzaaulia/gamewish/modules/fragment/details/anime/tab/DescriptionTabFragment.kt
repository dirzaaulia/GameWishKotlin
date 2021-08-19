package com.dirzaaulia.gamewish.modules.fragment.details.anime.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.databinding.FragmentDescriptionTabBinding
import com.dirzaaulia.gamewish.modules.fragment.details.anime.AnimeDetailsViewModel
import com.dirzaaulia.gamewish.util.capitalizeWords
import com.dirzaaulia.gamewish.util.htmlToTextFormatter
import com.dirzaaulia.gamewish.util.textDateFormatter2
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class DescriptionTabFragment : Fragment() {

    private lateinit var binding : FragmentDescriptionTabBinding
    private val viewModel : AnimeDetailsViewModel by viewModels()

    companion object {
        fun newInstance(details: Details) : DescriptionTabFragment {
            val args = Bundle()

            args.putParcelable(ARGS_DETAILS, details)

            val fragment = DescriptionTabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDescriptionTabBinding.inflate(inflater, container, false)

        setupView(arguments?.getParcelable(ARGS_DETAILS)!!)
        return binding.root
    }

    private fun setupView(detail: Details) {
        val alternativeEnglishTitle = detail.alternativeTitles?.en

        if (alternativeEnglishTitle == null || alternativeEnglishTitle.isEmpty()) {
            binding.animeDetailsTitleEn.text = detail.title
        } else {
            binding.animeDetailsTitleEn.text = alternativeEnglishTitle
        }

        binding.animeDetailsTitleJp.text = detail.alternativeTitles?.ja

        val author = StringBuilder()
        detail.authors?.forEach {
            author.append(String.format("%s %s (%s)", it.node?.firstName, it.node?.lastName, it.role))
            author.append("\n")
        }
        val authorString = author.toString().trim()

        if (detail.authors.isNullOrEmpty()) {
            binding.animeDetailsAuthor.isVisible = false
        } else {
            binding.animeDetailsAuthor.text = authorString
        }

        val startDate = detail.startDate
        val endDate = detail.endDate

        if (endDate != null) {
            val value = String.format("%s - %s", startDate?.let { textDateFormatter2(it) }, textDateFormatter2(endDate))
            binding.animeDetailsDate.text = value
        } else {
            val value = String.format("%s - now", startDate?.let { textDateFormatter2(it) })
            binding.animeDetailsDate.text = value
        }

        val rating = detail.rating
        if (rating != null || rating != "") {
            when (rating) {
                "g" ->  { binding.animeDetailsRating.text = String.format("G - All Ages") }
                "pg" -> { binding.animeDetailsRating.text = String.format("PG - Children") }
                "pg_13" -> { binding.animeDetailsRating.text = String.format("PG-13 - Teens 13 or older") }
                "r" -> { binding.animeDetailsRating.text = String.format("17+ (violence & profanity)") }
                "r+" -> { binding.animeDetailsRating.text = String.format("R+ - Mild Nudity") }
                "rx" -> { binding.animeDetailsRating.text = String.format("Rx - Hentai") }
            }
        }

        var source = detail.source
        source = source?.replace("_", " ")
        source = source?.capitalizeWords()

        if (detail.source.isNullOrEmpty()) {
            binding.animeDetailsSource.isVisible = false
        } else {
            binding.animeDetailsSource.text = String.format("Source : $source")
        }

        val genre = StringBuilder()
        detail.genres.forEach {
            genre.append(it.name)
            genre.append("  ")
        }
        binding.animeDetailsGenre.text = genre

        val synopsis = detail.synopsis
        if (synopsis != null) {
            binding.animeDetailsSynopsis.text = htmlToTextFormatter(synopsis)
            binding.animeDetailsSynopsisLabel.isVisible = true
        } else {
            binding.animeDetailsSynopsis.isVisible = false
            binding.animeDetailsSynopsisLabel.isVisible = false
        }

        val background = detail.background
        if (background != null) {
            binding.animeDetailsBackground.text = htmlToTextFormatter(background)
            binding.animeDetailsBackgroundLabel.isVisible = true
        } else {
            binding.animeDetailsBackground.isVisible = false
            binding.animeDetailsBackgroundLabel.isVisible = false
        }
    }
}