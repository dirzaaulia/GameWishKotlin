package com.dirzaaulia.gamewish.modules.activity.webview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor(
    private val repository: MyAnimeListRepository,
    private val protoRepository: ProtoRepository
) : ViewModel() {

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken : LiveData<String>
        get() = _refreshToken

    private val _expiresIn = MutableLiveData<Int>()
    val expiresIn : LiveData<Int>
        get() = _expiresIn

    private val _tokenResponse = MutableLiveData<MyAnimeListTokenResponse>()
    val tokenResponse : LiveData<MyAnimeListTokenResponse>
        get() = _tokenResponse

    fun getMyAnimeListToken(clientId : String, code : String, codeVerifier : String, grantType : String) {
        viewModelScope.launch {
            try {
                val response = repository.getMyAnimeListToken(clientId, code, codeVerifier, grantType)
                response.collect {
                    if (it.accessToken != null) {
                        _tokenResponse.value = it
                        _accessToken.value = it.accessToken!!
                        _refreshToken.value = it.refreshToken!!
                        _expiresIn.value = it.expiresIn!!

                        protoRepository.updateMyAnimeListAccessToken(it.accessToken)
                        protoRepository.updateMyAnimeListRefreshToken(it.refreshToken)
                        protoRepository.updateMyAnimeListExpresIn(it.expiresIn)
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }
}