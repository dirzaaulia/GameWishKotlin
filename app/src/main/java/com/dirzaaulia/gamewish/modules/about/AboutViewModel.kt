package com.dirzaaulia.gamewish.modules.about

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
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


    fun getSavedMyAnimeListToken() {
        viewModelScope.launch {
           userPreferencesFlow.collect {
               Timber.i("accessToken : %s\nrefreshToken : %s\nexpiresIn : %d", it
                   .accessToken, it.refreshToken, it.expiresIn)
               _accessToken.value = it.accessToken
           }
        }
    }

    fun getMyAnimeListUsername() {
        viewModelScope.launch {
            accessToken.value?.let { accessToken ->
                myAnimeListRepository.getMyAnimeListUsername(accessToken).collect {
                    _myAnimeListUser.value = it
                }
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