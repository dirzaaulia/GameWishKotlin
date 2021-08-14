package com.dirzaaulia.gamewish.modules.fragment.about

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.util.MYANIMELIST_CLIENT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class AboutViewModel  @Inject constructor(
    private val myAnimeListRepository: MyAnimeListRepository,
    private val protoRepository: ProtoRepository
) : ViewModel()  {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _myAnimeListUser = MutableLiveData<User>()
    val myAnimeListUser : LiveData<User>
        get() = _myAnimeListUser

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage


    fun getSavedMyAnimeListToken() {
        viewModelScope.launch {
           userPreferencesFlow.collect {
               Timber.i("accessToken : %s\nrefreshToken : %s\nexpiresIn : %d", it
                   .accessToken, it.refreshToken, it.expiresIn)
               _accessToken.value = it.accessToken
           }
        }
    }

    fun getMyAnimeListUsername(accessToken: String) {
        viewModelScope.launch {
            myAnimeListRepository.getMyAnimeListUsername(accessToken).collect {
                if (it.error == null) {
                    _myAnimeListUser.value = it
                } else {
                    userPreferencesFlow.collect { preference ->
                        getMyAnimeListRefreshToken(preference.refreshToken)
                    }
                }
            }
        }
    }

    private fun getMyAnimeListRefreshToken(refreshToken : String) {
        viewModelScope.launch {
            try {
                val response = myAnimeListRepository.
                getMyAnimeListRefreshToken(MYANIMELIST_CLIENT_ID, refreshToken)

                response.collect {
                    if (it.accessToken != null) {
                        _accessToken.value = it.accessToken.toString()

                        protoRepository.updateMyAnimeListAccessToken(it.accessToken.toString())
                        protoRepository.updateMyAnimeListRefreshToken(it.refreshToken.toString())
                        protoRepository.updateMyAnimeListExpresIn(it.expiresIn!!)
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                _errorMessage.value = "Something went wrong when getting data from MyAnimeList. " +
                        "Please try it again later!"
            }
        }
    }

    fun unlinkMyAnimeList() {
        viewModelScope.launch {
            protoRepository.updateMyAnimeListAccessToken("")
            protoRepository.updateMyAnimeListRefreshToken("")
            protoRepository.updateMyAnimeListExpresIn(0)
        }
    }
}