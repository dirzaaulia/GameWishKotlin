package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.DialogSeasonPickerBinding
import com.dirzaaulia.gamewish.util.SEASON_PICKER_DIALOG_KEY
import com.dirzaaulia.gamewish.util.capitalizeWords
import java.util.*

class SeasonPickerDialog : DialogFragment() {

    private lateinit var binding : DialogSeasonPickerBinding
    private lateinit var listener : SeasonPickerDialogListener
    private lateinit var season : String

    interface SeasonPickerDialogListener {
        fun updateSeasonalAnime(season: String, year: String)
    }

    fun setSeasonPickerDialogListener(listener: SeasonPickerDialogListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        setStyle(STYLE_NO_FRAME, R.style.ThemeOverlay_GameWish_Dialog)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        season = arguments?.getString(SEASON_PICKER_DIALOG_KEY) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSeasonPickerBinding.inflate(inflater, container, false)

        setupView()
        setupOnClickListeners()

        return binding.root
    }

    private fun setupView() {
        val arraySeason = arrayOf("Winter", "Summer", "Spring", "Fall")

        binding.spinnerSeason.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                arraySeason.map { it }
            )
        )

        val seasonSplit = season.split(" ")

        binding.spinnerSeason.setText(seasonSplit[0].capitalizeWords(), false)

        binding.year.setText(seasonSplit[1])
    }

    private fun setupOnClickListeners() {
        binding.ok.setOnClickListener {
            val season = binding.spinnerSeason.text.toString()
            val year = binding.year.text

            listener.updateSeasonalAnime(season, year.toString())
            dismiss()
        }
    }
}