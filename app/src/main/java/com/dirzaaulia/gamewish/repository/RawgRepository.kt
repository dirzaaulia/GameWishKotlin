package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.response.ScreenshotsResponse
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.network.rawg.RawgPagingSource
import com.dirzaaulia.gamewish.network.rawg.RawgService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RawgRepository @Inject constructor(private val service: RawgService) {

    fun refreshSearchGames(search: String) : Flow<PagingData<Games>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { RawgPagingSource(service, search) }
        ).flow
    }

    suspend fun getGameDetails(gameId : Int) : GameDetails {
        return withContext(Dispatchers.IO) {
            service.getGameDetails(gameId, RAWG_KEY)
        }
    }

    suspend fun getGameDetailsScreenshots(gameId: Int) : ScreenshotsResponse {
        return withContext(Dispatchers.IO) {
            service.getGameDetailsScreenshots(gameId, RAWG_KEY)
        }
    }

    companion object {
        private const val GAMES_PAGE_SIZE = 10
        private const val RAWG_KEY = "b1ece946fa424f4f887871a867743d0e"
    }
}