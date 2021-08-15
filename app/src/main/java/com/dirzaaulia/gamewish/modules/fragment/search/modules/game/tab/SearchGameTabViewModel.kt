package com.dirzaaulia.gamewish.modules.fragment.search.modules.game.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.rawg.Genre
import com.dirzaaulia.gamewish.data.models.rawg.Platform
import com.dirzaaulia.gamewish.data.models.rawg.Publisher
import com.dirzaaulia.gamewish.repository.RawgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchGameTabViewModel @Inject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    private var currentGenres : Flow<PagingData<Genre>>? = null
    private var currentPublishers : Flow<PagingData<Publisher>>? = null
    private var currentPlatforms : Flow<PagingData<Platform>>? = null

    fun getGenres() : Flow<PagingData<Genre>> {
        val lastResult = currentGenres

        if (lastResult != null) {
            return lastResult
        }
        val newResult : Flow<PagingData<Genre>> = repository.getGenres().cachedIn(viewModelScope)

        currentGenres = newResult

        return newResult
    }

    fun getPublishers() : Flow<PagingData<Publisher>> {
        val lastResult = currentPublishers

        if (lastResult != null) {
            return lastResult
        }

        val newResult : Flow<PagingData<Publisher>> = repository.getPublishers().cachedIn(viewModelScope)

        currentPublishers = newResult

        return newResult
    }

    fun getPlatforms() : Flow<PagingData<Platform>> {
        val lastResult = currentPlatforms

        if (lastResult != null) {
            return lastResult
        }

        val newResult : Flow<PagingData<Platform>> = repository.getPlatforms().cachedIn(viewModelScope)

        currentPlatforms = newResult

        return newResult
    }
}