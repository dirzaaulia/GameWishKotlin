package com.dirzaaulia.gamewish.modules.activity.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityWebViewBinding
import com.dirzaaulia.gamewish.util.*
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.HttpUrl
import timber.log.Timber

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

    private val binding: ActivityWebViewBinding by contentView(R.layout.activity_web_view)

    private val viewModel: WebViewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWebView()
        subscribeToken()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.apply {
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

            webview.settings.javaScriptEnabled = true

            webview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val urlValue = request?.url.toString()
                    val code = request?.url?.getQueryParameter("code")

                    urlValue.let {
                        Timber.i(it)

                        if (urlValue.contains(MYANIMELIST_BASE_URL_CALLBCK)) {
                            if (code != null) {
                                viewModel.getMyAnimeListToken(
                                    MYANIMELIST_CLIENT_ID,
                                    code,
                                    MYANIMELIST_CODE_CHALLENGE,
                                    "authorization_code")
                            }
                            return true
                        }
                    }
                    return false
                }
            }

            webview.loadUrl(url.toString())
        }
    }

    private fun subscribeToken() {
        viewModel.accessToken.observe(this) {
            if (it != null) {
                val intent = Intent().apply {
                    putExtra("tokenResponse", viewModel.tokenResponse.value)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}