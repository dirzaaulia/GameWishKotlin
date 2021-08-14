package com.dirzaaulia.gamewish.modules.fragment.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.rawg.Games
import com.dirzaaulia.gamewish.repository.RawgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    var currentQueryValue: String? = null
    var currentGenresValue: Int? = null
    var currentPublishersValue: Int? = null
    var currentPlatformsValue: Int? = null

    private var currentSearchGamesResult: Flow<PagingData<Games>>? = null

    private val _genre = MutableLiveData<Int>()
    val genre : LiveData<Int>
        get() = _genre

    private val _publisher = MutableLiveData<Int>()
    val publisher : LiveData<Int>
        get() = _publisher

    private val _platform = MutableLiveData<Int>()
    val platforms : LiveData<Int>
        get() = _platform

    fun refreshSearchGames(request: String?, genres: Int?, publishers: Int?, platforms: Int?) :
            Flow<PagingData<Games>> {
        val lastResult = currentSearchGamesResult
        if (request == currentQueryValue && genres == currentGenresValue &&
            publishers == currentPublishersValue && lastResult != null) {
            return lastResult
        }

        currentQueryValue = request
        currentGenresValue = genres
        currentPublishersValue = publishers
        currentPlatformsValue = platforms

        val newResult : Flow<PagingData<Games>> =
            repository.refreshSearchGames(request, genres, publishers, platforms).cachedIn(viewModelScope)

        currentSearchGamesResult = newResult

        return newResult
    }

    fun updateGenre(genre : Int) {
        _genre.value = genre
    }

    fun updatePublisher(publisher: Int) {
        _publisher.value = publisher
    }

    fun updatePlatform(platform : Int) {
        _platform.value = platform
    }
}