package com.dirzaaulia.gamewish.modules.activity.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.databinding.ActivityLoginBinding
import com.dirzaaulia.gamewish.modules.activity.main.MainActivity
import com.dirzaaulia.gamewish.util.GOOGLE_SIGN_IN_WEB_CLIENT_ID
import com.dirzaaulia.gamewish.util.contentView
import com.dirzaaulia.gamewish.util.showAlertDialogWithoutButton
import com.dirzaaulia.gamewish.util.showSnackbarShort
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by contentView(R.layout.activity_login)
    private val viewModel : LoginViewModel by viewModels()

    private val openPostActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this::onSignInResult)

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeGoogleUserLoginStatus()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_SIGN_IN_WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignIn = GoogleSignIn.getClient(this, gso)

        setupView(googleSignIn)
    }

    private fun onSignInResult(result : ActivityResult) {
        if (result.resultCode == RESULT_OK) {
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

    private fun subscribeGoogleUserLoginStatus() {
        auth = viewModel.getFirebaseAuth()
        viewModel.getGoogleLoginStatus()
        viewModel.googleLoginStatus.observe(this) {
            when (it) {
                true -> {
                    viewModel.setLocalDataStatus(false)
                    goToMainActivity()
                }
            }
        }
    }

    private fun setupView(googleSignIn: GoogleSignInClient) {
        binding.loginButton.setOnClickListener {
            login(googleSignIn)
        }

        binding.loginInfo.setOnClickListener {
            showAlertDialogWithoutButton(
                this,
                "Google Login",
                "Login with Google Account will save your data into your Google Account." +
                        " So when next time your device changes or you reinstalling GameWish," +
                        " you just login again with your Google Account and you will see your old data.")
        }

        binding.loginSkip.setOnClickListener {
            viewModel.setLocalDataStatus(true)
            goToMainActivity()
        }
    }

    private fun login(googleSignIn: GoogleSignInClient) {
        val signInIntent = googleSignIn.signInIntent
        openPostActivity.launch(signInIntent)
    }

    private fun authenticateGoogleLogin(credential: AuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Timber.i("Google Login Success | User %s", auth.currentUser)
                viewModel.setLocalDataStatus(false)
                goToMainActivity()
            } else {
                Timber.w("Google Login Failure | Error : %s", it.exception.toString())
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}