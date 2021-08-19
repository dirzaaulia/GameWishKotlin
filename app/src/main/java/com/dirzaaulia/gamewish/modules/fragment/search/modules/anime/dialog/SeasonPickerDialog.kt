package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.DialogSeasonPickerBinding
import java.util.*

class SeasonPickerDialog : DialogFragment() {

    private lateinit var binding : DialogSeasonPickerBinding
    private lateinit var listener : SeasonPickerDialogListener

    interface SeasonPickerDialogListener {
        fun updateSeasonalAnime(season: String, year: String)
    }

    fun setSeasonPickerDialogListener(listener: SeasonPickerDialogListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        setStyle(STYLE_NO_FRAME, R.style.ThemeOverlay_GameWish_MaterialAlertDialog)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSeasonPickerBinding.inflate(inflater, container, false)

        setupView()
        setupOnClickListeners()

        return binding.root
    }

    private fun setupView() {
        val season = arrayOf("Winter", "Summer", "Spring", "Fall")

        binding.spinnerSeason.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                season.map { it }
            )
        )

        binding.spinnerSeason.setText(getString(R.string.winter), false)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        binding.year.setText(currentYear.toString())

    }

    private fun setupOnClickListeners() {
        binding.ok.setOnClickListener {
            val season = binding.spinnerSeason.text.toString()
            val year = binding.year.text.toString()

            listener.updateSeasonalAnime(season, year)
            dismiss()
        }
    }
}