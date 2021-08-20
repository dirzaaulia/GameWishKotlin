package com.dirzaaulia.gamewish.modules.fragment.about

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.databinding.FragmentAboutBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.modules.activity.webview.WebViewActivity
import com.dirzaaulia.gamewish.util.*
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private lateinit var binding : FragmentAboutBinding
    private val viewModel : AboutViewModel by viewModels()
    private var accessToken : String? = null
    private var googleLoginStatus : Boolean = false

    private val openPostActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        this::onMyAnimeListLogin)

    private val openPostActivityGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this::onSignInResult)

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
        subscribeGoogleLoginStatus()
        subscribeAccessToken()
        setupView()
        subscribeErrorMessage()
    }

    private fun subscribeGoogleLoginStatus() {
        viewModel.getGoogleLoginStatus()
        viewModel.googleStatus.observe(viewLifecycleOwner) {
            Timber.i("Google Status : %s", it.toString())
            googleLoginStatus = it
            Timber.i("Google Login Status : %s", googleLoginStatus.toString())
            val auth = viewModel.getFirebaseAuth()
            when (it) {
                true -> {
                    binding.aboutGoogleImage.isVisible = true
                    binding.aboutGoogleUsername.isVisible = true

                    setImageWithGlide(binding.aboutGoogleImage, auth.currentUser?.photoUrl.toString())
                    binding.aboutGoogleUsername.text = String.format(
                        "Google Account Linked : ${auth.currentUser?.displayName}")
                    binding.aboutGoogleButton.text = String.format("Unlink Google Account")
                }
                false -> {
                    binding.aboutGoogleImage.isVisible = false
                    binding.aboutGoogleUsername.isVisible = true
                    binding.aboutGoogleUsername.text = getString(R.string.google_account_is_not_linked)
                    binding.aboutGoogleButton.text = String.format("Link Google Account")
                }
            }
        }
    }

    private fun subscribeAccessToken() {
        viewModel.getSavedMyAnimeListToken()
        viewModel.accessToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.aboutButtonMyanimelistLink.isVisible = true
                binding.aboutButtonMyanimelistLink.text = getString(R.string.link_myanimelist)

                binding.aboutMyanimelistImage.isVisible = false
                binding.aboutMyanimelistLinkText.isVisible = false

                binding.aboutMyanimelistProgressBar.isVisible = false
            } else {
                accessToken = it
                binding.aboutButtonMyanimelistLink.text = getString(R.string.unlink_myanimelist)
                subscribeMyAnimeListUser(it)
            }
        }
    }

    private fun subscribeMyAnimeListUser(accessToken: String) {
        Timber.i("subscribeMyAnimeListUser")
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
                binding.aboutMyanimelistProgressBar.isVisible = false

                val value = String.format("MyAnimeList Account Linked : ${it.name}")
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
                accessToken = ""
                viewModel.unlinkMyAnimeList()
            }
        }

        binding.aboutGoogleButton.setOnClickListener {
            when (googleLoginStatus) {
                true -> {
                    googleSignOut()
                }
                else -> {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(GOOGLE_SIGN_IN_WEB_CLIENT_ID)
                        .requestEmail()
                        .build()

                    val googleSignIn = GoogleSignIn.getClient(requireContext(), gso)

                    this.googleSignIn(googleSignIn)
                }
            }
        }
    }

    private fun subscribeErrorMessage() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showSnackbarShort(binding.root, it)

                val progressBarMyAnimeListIsVisible = binding.aboutMyanimelistProgressBar.isVisible
                if (progressBarMyAnimeListIsVisible) {
                    binding.aboutMyanimelistProgressBar.isVisible = false
                }
            }
        }
    }

    private fun googleSignIn(googleSignIn: GoogleSignInClient) {
        val signInIntent = googleSignIn.signInIntent
        openPostActivityGoogle.launch(signInIntent)
    }


    private fun googleSignOut() {
        val auth = viewModel.getFirebaseAuth()
        auth.signOut()
        viewModel.getGoogleLoginStatus()
        viewModel.setLocalDataStatus(true)
    }

    private fun onSignInResult(result : ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                val idToken = account.idToken
                val credential = viewModel.authGoogleLogin(idToken!!)
                authenticateGoogleLogin(credential)
            } else {
                Timber.i("Response is NULL")
            }
        } else {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isCanceled) {
                showSnackbarShort(binding.root, "Google login is canceled")
            } else {
                showSnackbarShort(binding.root, "Google login is error ${task.exception.toString()}")
            }
        }
    }

    private fun authenticateGoogleLogin(credential: AuthCredential) {
        val auth = viewModel.getFirebaseAuth()
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful) {
                Timber.i("Google Login Success | User %s", auth.currentUser)
                viewModel.setLocalDataStatus(false)
                viewModel.getGoogleLoginStatus()
            } else {
                Timber.w("Google Login Failure | Error : %s", it.exception.toString())
                showSnackbarShortWithAnchor(
                    requireActivity().findViewById(R.id.bottom_nav), requireActivity().findViewById(R.id.bottom_nav),
                    "Google Login Failure | Error : ${it.exception.toString()}"
                )
            }
        }
    }

    private fun onMyAnimeListLogin(result : ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            binding.aboutMyanimelistImage.isVisible = false
            binding.aboutMyanimelistLinkText.isVisible = false

            binding.aboutMyanimelistProgressBar.isVisible = true

            subscribeAccessToken()
        } else {
            showSnackbarShortWithAnchor(requireActivity().findViewById(R.id.bottom_nav), requireActivity()
                .findViewById(R.id.bottom_nav), "You canceled MyAnimeList Account link!")
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
