package com.dirzaaulia.gamewish.modules.search.tab.modules

import androidx.lifecycle.*
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
class SearchTabViewModel @Inject constructor(
    private val repository: RawgRepository
) : ViewModel() {

    var currentGenres : Flow<PagingData<Genre>>? = null
    var currentPublishers : Flow<PagingData<Publisher>>? = null
    var currentPlatforms : Flow<PagingData<Platform>>? = null

    fun getGenres() : Flow<PagingData<Genre>> {
        val newResult : Flow<PagingData<Genre>> = repository.getGenres().cachedIn(viewModelScope)

        currentGenres = newResult

        return newResult
    }

    fun getPublishers() : Flow<PagingData<Publisher>> {
        val newResult : Flow<PagingData<Publisher>> = repository.getPublishers().cachedIn(viewModelScope)

        currentPublishers = newResult

        return newResult
    }

    fun getPlatforms() : Flow<PagingData<Platform>> {
        val newResult : Flow<PagingData<Platform>> = repository.getPlatforms().cachedIn(viewModelScope)

        currentPlatforms = newResult

        return newResult
    }
}