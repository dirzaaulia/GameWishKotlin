package com.dirzaaulia.gamewish.modules.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.repository.RawgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel @ViewModelInject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    var currentSearchGamesResult: Flow<PagingData<Games>>? = null

    fun refreshSearchGames(request: String): Flow<PagingData<Games>> {
        val newResult: Flow<PagingData<Games>> =
            repository.refreshSearchGames(request).cachedIn(viewModelScope)

        currentSearchGamesResult = newResult

        return newResult
    }
}