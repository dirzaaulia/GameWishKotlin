package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.network.rawg.RawgPagingSource
import com.dirzaaulia.gamewish.network.rawg.RawgService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RawgRepository @Inject constructor(private val service: RawgService) {

    fun refreshSearchGames(search: String) : Flow<PagingData<Games>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { RawgPagingSource(service, search) }
        ).flow
    }

    companion object {
        private const val GAMES_PAGE_SIZE = 10
    }
}