package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime.tab

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
class SearchAnimeTabViewModel @Inject constructor(
    private val myAnimeListRepository: MyAnimeListRepository,
    private val protoRepository: ProtoRepository
) : ViewModel() {

    private var currentQueryValue: String? = null
    private var currentYearValue : String? = null
    private var currentSeasonValue : String? = null

    private var currentSearchAnimeResult : Flow<PagingData<ParentNode>>? = null
    private var currentSearchMangaResult : Flow<PagingData<ParentNode>>? = null
    private var currentSearchSeasonalResult : Flow<PagingData<ParentNode>>? = null

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage


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

        if (query == currentQueryValue && currentSearchAnimeResult != null) {
            Timber.i("return Last Anime")
            return lastAnimeResult
        }

        currentQueryValue = query

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.searchMyAnimeListAnime(authorization, query).cachedIn(viewModelScope)

        currentSearchAnimeResult = newResult

        return newResult
    }

    fun refreshSearchManga(authorization : String, query: String) :
            Flow<PagingData<ParentNode>>? {
        val lastMangaResult = currentSearchMangaResult

        if (query == currentQueryValue && currentSearchMangaResult != null) {
            Timber.i("return Last Manga")
            return lastMangaResult
        }

        currentQueryValue = query

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