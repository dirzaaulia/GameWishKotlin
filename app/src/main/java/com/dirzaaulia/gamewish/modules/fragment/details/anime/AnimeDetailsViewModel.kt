package com.dirzaaulia.gamewish.modules.fragment.details.anime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.gamewish.data.models.myanimelist.Details
import com.dirzaaulia.gamewish.data.models.myanimelist.ListStatus
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.util.MYANIMELIST_CLIENT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val myAnimeListRepository: MyAnimeListRepository,
    private val protoRepository: ProtoRepository
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private val _itemDetails = MutableLiveData<Details>()
    val itemDetails : LiveData<Details>
        get() = _itemDetails

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken : LiveData<String>
        get() = _refreshToken

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage

    private val _updateResponse = MutableLiveData<ListStatus?>()
    val updateResponse : LiveData<ListStatus?>
        get() = _updateResponse

    fun getSavedMyAnimeListToken() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                Timber.i("accessToken : %s\nrefreshToken : %s\nexpiresIn : %d", it
                    .accessToken, it.refreshToken, it.expiresIn)
                _accessToken.value = it.accessToken
                _refreshToken.value = it.refreshToken
            }
        }
    }

    fun getAnimeDetails(accessToken: String, animeId: String) {
        viewModelScope.launch {
            try {
                val response = myAnimeListRepository.getMyAnimeListAnimeDetails(accessToken, animeId)
                response.collect {
                    if (it.id != null) {
                        _itemDetails.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val status = e.message.toString()

                if (status.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun getMangaDetails(accessToken: String, animeId : String) {
        viewModelScope.launch {
            try {
                val response = myAnimeListRepository.getMyAnimeListMangaDetails(accessToken, animeId)
                response.collect {
                    if (it.id != null) {
                        _itemDetails.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val status = e.message.toString()

                if (status.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
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

    fun updateMyAnimeListAnimeList(
        authorization: String, animeId : Int, status : String, isRewatching : Boolean?, score : Int?, episode : Int?) {
        viewModelScope.launch {
            try {
                val response =  myAnimeListRepository.updateMyAnimeListAnimeList(
                    authorization, animeId, status, isRewatching, score, episode)
                response.collect {
                    if (it.status != null) {
                        _updateResponse.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun deleteMyAnimeListAnimeList(authorization: String, animeId : Int) {
        viewModelScope.launch {
            try {
                myAnimeListRepository.deleteMyAnimeListAnimeList(authorization, animeId)
                _updateResponse.value = null
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun updateMyAnimeListMangaList(
        authorization: String, mangaId : Int, status : String, isRewatching : Boolean?, score : Int?, episode : Int?) {
        viewModelScope.launch {
            try {
                val response =  myAnimeListRepository.updateMyAnimeListMangaList(
                    authorization, mangaId, status, isRewatching, score, episode)
                response.collect {
                    if (it.status != null) {
                        _updateResponse.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun deleteMyAnimeListMangaList(authorization: String, mangaId : Int) {
        viewModelScope.launch {
            try {
                myAnimeListRepository.deleteMyAnimeListMangaList(authorization, mangaId)
                _updateResponse.value = null
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }
}