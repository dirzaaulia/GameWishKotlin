package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.util.MYANIMELIST_CLIENT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchAnimeViewModel @Inject constructor(
    private val myAnimeListRepository: MyAnimeListRepository,
    private val protoRepository: ProtoRepository
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private var currentAnimeQueryValue: String? = null
    private var currentMangaQueryValue: String? = null
    private var currentYearValue : String? = null
    private var currentSeasonValue : String? = null

    private var currentSearchAnimeResult : Flow<PagingData<ParentNode>>? = null
    private var currentSearchMangaResult : Flow<PagingData<ParentNode>>? = null
    private var currentSearchSeasonalResult : Flow<PagingData<ParentNode>>? = null

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery : LiveData<String>
        get() = _searchQuery

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken : LiveData<String>
        get() = _refreshToken

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage

    private val _season = MutableLiveData<String>()
    val season : LiveData<String>
        get() = _season

    init {
        getSavedMyAnimeListToken()
        setSearchQuery("")
    }

    private fun getSavedMyAnimeListToken() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                Timber.i("accessToken : %s\nrefreshToken : %s\nexpiresIn : %d", it
                    .accessToken, it.refreshToken, it.expiresIn)
                _accessToken.value = it.accessToken
                _refreshToken.value = it.refreshToken
            }
        }
    }

    fun setSeason(season : String) {
        _season.value = season
    }

    fun setSearchQuery(query : String) {
        _searchQuery.value = query
    }

    fun getMyAnimeListRefreshToken(refreshToken : String) {
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

    fun refreshSearchAnime(authorization : String, query: String) :
            Flow<PagingData<ParentNode>>? {
        val lastAnimeResult = currentSearchAnimeResult

        if (query == currentAnimeQueryValue && currentSearchAnimeResult != null) {
            Timber.i("return Last Anime")
            return lastAnimeResult
        }

        currentAnimeQueryValue = query

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.searchMyAnimeListAnime(authorization, query).cachedIn(viewModelScope)

        currentSearchAnimeResult = newResult

        return newResult
    }

    fun refreshSearchManga(authorization : String, query: String) :
            Flow<PagingData<ParentNode>>? {
        val lastMangaResult = currentSearchMangaResult

        if (query == currentMangaQueryValue && currentSearchMangaResult != null) {
            Timber.i("return Last Manga")
            return lastMangaResult
        }

        currentMangaQueryValue = query

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.searchMyAnimeListManga(authorization, query).cachedIn(viewModelScope)

        currentSearchMangaResult = newResult

        return newResult
    }

    fun refreshSeasonal(authorization: String, year : String, season : String): Flow<PagingData<ParentNode>>? {
        val lastSeasonalResult = currentSearchSeasonalResult

        if (year == currentYearValue && season == currentSeasonValue && currentSearchSeasonalResult != null) {
            Timber.i("return Last Seasonal")
            return lastSeasonalResult
        }

        currentYearValue = year
        currentSeasonValue = season

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.getMyAnimeListSeasonal(authorization,year, season).cachedIn(viewModelScope)

        currentSearchSeasonalResult = newResult

        return newResult
    }
}
