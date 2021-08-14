package com.dirzaaulia.gamewish.modules.fragment.about

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.databinding.FragmentAboutBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private lateinit var binding : FragmentAboutBinding
    private val viewModel : AboutViewModel by viewModels()
    private var accessToken : String? = null
    private val openPostActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.let { intentData ->
                    val value = intentData.getParcelableExtra<MyAnimeListTokenResponse>("tokenResponse")
                    value?.accessToken?.let{
                        showSnackbarShort(binding.root, it)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAboutBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.aboutToolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAccessToken()
        setupView()
        subscribeErrorMessage()
    }

    private fun subscribeAccessToken() {
        viewModel.getSavedMyAnimeListToken()
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.aboutButtonMyanimelistLink.isVisible = true
                binding.aboutButtonMyanimelistLink.text = getString(R.string.link_myanimelist)

                binding.aboutMyanimelistImage.isVisible = false
                binding.aboutMyanimelistLinkText.isVisible = false
            } else {
                binding.aboutButtonMyanimelistLink.text = getString(R.string.unlink_myanimelist)
                subscribeMyAnimeListUser(it)
            }
        }
    }

    private fun subscribeMyAnimeListUser(accessToken: String) {
        Timber.i("subscribeMyAnimeListUser")
        binding.aboutMyanimelistProgressBar.isVisible = true
        viewModel.getMyAnimeListUsername(accessToken)
        viewModel.myAnimeListUser.observe(viewLifecycleOwner) {
            binding.aboutMyanimelistProgressBar.isVisible = false

            if (it.id.isNullOrEmpty()) {
                val value = String.format(
                    "Something when wrong when getting MyAnimeListData! Please try it again")
                binding.aboutMyanimelistLinkText.text = value
                binding.aboutMyanimelistLinkText.isVisible = true

                binding.aboutMyanimelistImage.isVisible = false
            } else {
                val value = String.format("MyAnimeList Account Linked : %s", it.name)
                binding.aboutMyanimelistLinkText.text = value
                binding.aboutMyanimelistLinkText.isVisible = true

                binding.aboutMyanimelistImage.isVisible = true
                setImageWithGlide(binding.aboutMyanimelistImage, it.picture)
            }
        }
    }

    private fun setupView() {
        binding.aboutApp.setOnClickListener {
            openLink(requireContext(),"https://linktr.ee/DirzaAulia")
        }

        binding.aboutApp2.setOnClickListener {
            openLink(requireContext(), "https://play.google.com/store/apps/dev?id=4806849608818858118")
        }

        binding.aboutGames.setOnClickListener {
            openLink(requireContext(), "https://www.rawg.io")
        }

        binding.aboutButtonSendEmail.setOnClickListener {
            sendEmail()
        }

        binding.aboutMyanimelist.setOnClickListener {
            openLink(requireContext(), MYANIMELIST_BASE_URL)
        }

        binding.aboutButtonMyanimelistLink.setOnClickListener {
            if (accessToken.isNullOrEmpty()) {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                openPostActivity.launch(intent)
            } else {
                viewModel.unlinkMyAnimeList()
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                showSnackbarShort(binding.root, it)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:dirzaaulia11@gmail.com" )
        }

        startActivity(intent)
    }
}