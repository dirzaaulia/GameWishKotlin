package com.dirzaaulia.gamewish.modules.activity.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityWebViewBinding
import com.dirzaaulia.gamewish.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.HttpUrl
import timber.log.Timber

const val MYANIMELIST_URL_LOGIN = "https://myanimelist.net/login.php?from=%2Fdialog%2Fauthorization"
const val MYANIMELIST_URL_LOGIN_2 = "https://myanimelist.net/dialog/authorization"

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

    private val binding: ActivityWebViewBinding by contentView(R.layout.activity_web_view)
    private val viewModel: WebViewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWebView()
        subscribeToken()
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else {
            val url = binding.webview.url
            Timber.i(url)
            if (url != null) {
                when {
                    url.contains(MYANIMELIST_URL_LOGIN, true) -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                    url.contains(MYANIMELIST_URL_LOGIN_2, true) -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                    else -> {
                        super.onBackPressed()
                    }
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(MYANIMELIST_URL)
            .addPathSegment("v1")
            .addPathSegment("oauth2")
            .addPathSegment("authorize")
            .addQueryParameter("response_type", "code")
            .addQueryParameter("client_id", MYANIMELIST_CLIENT_ID)
            .addQueryParameter("code_challenge", MYANIMELIST_CODE_CHALLENGE)
            .addQueryParameter("state", MYANIMELIST_STATE)
            .build()

        binding.webview.settings.javaScriptEnabled = true

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlValue = request?.url.toString()
                val code = request?.url?.getQueryParameter("code")
                val error = request?.url?.getQueryParameter("error")

                urlValue.let {
                    Timber.i(it)
                    if (urlValue.contains(MYANIMELIST_BASE_URL_CALLBACK)) {
                        if (code != null) {
                            viewModel.getMyAnimeListToken(
                                MYANIMELIST_CLIENT_ID,
                                code,
                                MYANIMELIST_CODE_CHALLENGE,
                                "authorization_code")
                        } else if (error != null) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                        return true
                    }
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressHorizontal.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressHorizontal.isVisible = false
            }
        }

        binding.webview.loadUrl(url.toString())
    }

    private fun subscribeToken() {
        viewModel.accessToken.observe(this) {
            if (it != null) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}