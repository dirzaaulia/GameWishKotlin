package com.dirzaaulia.gamewish.modules.search.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.repository.RawgRepository
import kotlinx.coroutines.flow.Flow

class SearchViewModel @ViewModelInject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    private var currentSearchGamesResult: Flow<PagingData<Games>>? = null

    fun refreshSearchGames(request: String): Flow<PagingData<Games>> {
        val newResult: Flow<PagingData<Games>> =
            repository.refreshSearchGames(request).cachedIn(viewModelScope)

        currentSearchGamesResult = newResult

        return newResult
    }
}