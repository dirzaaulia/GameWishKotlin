package com.dirzaaulia.gamewish.modules.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.FragmentAboutBinding
import com.dirzaaulia.gamewish.modules.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private lateinit var binding : FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAboutBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.aboutToolbar)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.aboutApp.setOnClickListener {
            openLink("https://linktr.ee/DirzaAulia")
        }

        binding.aboutApp2.setOnClickListener {
            openLink("https://play.google.com/store/apps/dev?id=4806849608818858118")
        }

        binding.aboutGames.setOnClickListener {
            openLink("https://www.rawg.io")
        }

        binding.aboutButtonSendEmail.setOnClickListener {
            sendEmail()
        }
    }

    private fun openLink(url : String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:dirzaaulia11@gmail.com" )
        }

        startActivity(intent)
    }
}