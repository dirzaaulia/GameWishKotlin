package com.dirzaaulia.gamewish.modules.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.repository.RawgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    private var currentQueryValue: String? = null

    var currentSearchGamesResult: Flow<PagingData<Games>>? = null

    fun refreshSearchGames(request: String): Flow<PagingData<Games>> {

        val lastResult = currentSearchGamesResult
        if (request == currentQueryValue && lastResult != null) {
            return lastResult
        }

        currentQueryValue = request

        val newResult : Flow<PagingData<Games>> = repository.refreshSearchGames(request).cachedIn(viewModelScope)

        currentSearchGamesResult = newResult

        return newResult

    }
}