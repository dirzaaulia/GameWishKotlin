package com.dirzaaulia.gamewish.modules.global.dialog

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.databinding.BottomSheetAnimeListBinding
import com.dirzaaulia.gamewish.util.capitalizeWords
import com.dirzaaulia.gamewish.util.lowerCaseWords
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.transition.MaterialContainerTransform
import timber.log.Timber

const val ARGS_DATA = "argsData"
const val ARGS_POSITION = "argsPosition"
const val ARGS_ISFROMSEARCH = "argsIsFromSearch"

class BottomSheetAnimeDialog : BottomSheetDialogFragment() {

    private lateinit var binding : BottomSheetAnimeListBinding
    private lateinit var parentNode: ParentNode
    private lateinit var listener : BottomSheetAnimeDialogListener

    private var position = 0
    private var isUpdating : Boolean = false
    private var status : String? = null
    private var score : Int = 0
    private var isFromSearch : Boolean = false

    companion object {
        fun newInstance(position : Int, parentNode: ParentNode?, isFromSearch : Boolean) : BottomSheetAnimeDialog {
            val args = Bundle()

            args.putInt(ARGS_POSITION, position)
            args.putParcelable(ARGS_DATA, parentNode)
            args.putBoolean(ARGS_ISFROMSEARCH, isFromSearch)

            val fragment = BottomSheetAnimeDialog()
            fragment.arguments = args
            return fragment
        }
    }

    interface BottomSheetAnimeDialogListener {
        fun updateAnimeList(
            animeId: Int,
            status: String,
            isRewatching: Boolean?,
            score: Int?,
            episode: Int?,
            isUpdating: Boolean
        )
        fun detailAnimeList(animeId: Int)
        fun deleteAnimeList(animeId: Int)
    }

    fun setBottomSheetAnimeDialogListener(listener: BottomSheetAnimeDialogListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetAnimeListBinding.inflate(inflater, container, false)

        position = arguments?.getInt(ARGS_POSITION) ?: 0
        parentNode = arguments?.getParcelable(ARGS_DATA)!!
        isFromSearch = arguments?.getBoolean(ARGS_ISFROMSEARCH) ?: false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupOnClickListeners()
    }

    private fun setupView() {
        val detail = parentNode
        binding.title.text = detail.node?.title

        val status : Array<String> =
            if (position == 0 || position == 1) {
                arrayOf("Watching", "Completed", "On Hold", "Dropped", "Plan To Watch")
            } else {
                arrayOf("Reading", "Completed", "On Hold", "Dropped", "Plan To Read")
            }

        binding.spinnerStatus.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                status.map { it }
            )
        )

        val score = arrayOf("(1) Appalling", "(2) Horrible", "(3) Bad", "(4) Very Bad", "(5) Average", "(6) Fine",
            "(7) Good" , "(8) Very Good", "(9) Great", "(10) Masterpiece")

        binding.spinnerScore.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                score.map { it }
            )
        )

        if (position == 0 || position == 1) {
            binding.fieldWatchedEpisode.hint = String.format("Episodes Watched")
            binding.switchRewatching.text = String.format("Is Rewatching")
        } else {
            binding.fieldWatchedEpisode.hint = String.format("Chapters Read")
            binding.switchRewatching.text = String.format("Is Rereading")
        }

        if (parentNode.listStatus != null) {
            isUpdating = true

            var statusValue = parentNode.listStatus?.status
            statusValue = statusValue?.replace("_", " ")
            statusValue = statusValue?.capitalizeWords()

            if (statusValue.equals("On Hold", true) || statusValue.equals("Plan To Watch", true)
                || statusValue.equals("Plan To Read", true)) {
                binding.spinnerScore.isEnabled = false
                binding.watchedEpisode.isEnabled = false
                binding.switchRewatching.isEnabled = false
            } else {
                binding.spinnerScore.isEnabled = true
                binding.watchedEpisode.isEnabled = true
                binding.switchRewatching.isEnabled = true
            }

            binding.spinnerStatus.setText(statusValue, false)

            val scoreValue = parentNode.listStatus?.score
            if (scoreValue == 0 || scoreValue == null) {
                binding.spinnerScore.setText(getString(com.dirzaaulia.gamewish.R.string.select_score), false)
            } else {
                binding.spinnerScore.setText(score[parentNode.listStatus?.score!!], false)
            }

            val episodes = parentNode.listStatus?.episodes
            val chapters = parentNode.listStatus?.chapters

            if (episodes != null) {
                binding.watchedEpisode.setText(episodes.toString())
            } else if (chapters != null) {
                binding.watchedEpisode.setText(chapters.toString())
            }

            val isRewatching = parentNode.listStatus?.isRewatching
            val isReReading = parentNode.listStatus?.isReReading
            when {
                isRewatching != null -> {
                    binding.switchRewatching.isChecked = isRewatching
                }
                isReReading != null -> {
                    binding.switchRewatching.isChecked = isReReading
                }
                else -> {
                    binding.switchRewatching.isVisible = false
                }
            }
        } else {
            isUpdating = false

            binding.buttonDelete.isVisible = false

            when (position) {
                0 -> binding.buttonUpdate.text = String.format("Add To Anime List")
                1 -> binding.buttonUpdate.text = String.format("Add To Anime List")
                2 -> binding.buttonUpdate.text = String.format("Add To Manga List")
            }
        }

        binding.buttonDetail.isVisible = !isFromSearch
    }

    private fun setupOnClickListeners() {
        binding.spinnerStatus.setOnItemClickListener { _, _, _, _ ->
            status = binding.spinnerStatus.text.toString()
            status = status?.lowerCaseWords()
            status = status?.replace(" ", "_")

            if (status.equals("on_hold", true) || status.equals("plan_to_watch", true)
                    || status.equals("plan_to_read", true)) {
                binding.spinnerScore.isEnabled = false
                binding.watchedEpisode.isEnabled = false
                binding.switchRewatching.isEnabled = false
            } else {
                binding.spinnerScore.isEnabled = true
                binding.watchedEpisode.isEnabled = true
                binding.switchRewatching.isEnabled = true
            }
        }

        binding.spinnerScore.setOnItemClickListener { _, _, position, _ ->
            score = position + 1
        }

        binding.buttonDelete.setOnClickListener {
            val animeId = parentNode.node?.id
            listener.deleteAnimeList(animeId!!)
            dismiss()
        }

        binding.buttonDetail.setOnClickListener {
            val animeId = parentNode.node?.id
            listener.detailAnimeList(animeId!!)
            dismiss()
        }

        binding.buttonUpdate.setOnClickListener {
            val animeId = parentNode.node?.id
            val isRewatching = binding.switchRewatching.isChecked
            val episodes = binding.watchedEpisode.text
            val episodeValue : Int = if (episodes.isNullOrEmpty()) {
                0
            } else {
                episodes.toString().toInt()
            }

            listener.updateAnimeList(animeId!!, status!!, isRewatching, score, episodeValue, isUpdating)
            dismiss()
        }
    }
}