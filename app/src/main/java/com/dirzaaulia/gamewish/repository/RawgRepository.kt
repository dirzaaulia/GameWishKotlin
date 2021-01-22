package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.models.Screenshots
import com.dirzaaulia.gamewish.data.response.ScreenshotsResponse
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.network.rawg.RawgPagingSource
import com.dirzaaulia.gamewish.network.rawg.RawgService
import com.dirzaaulia.gamewish.util.GAMES_PAGE_SIZE
import com.dirzaaulia.gamewish.util.RAWG_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RawgRepository @Inject constructor(private val service: RawgService) {

    fun refreshSearchGames(search: String) : Flow<PagingData<Games>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { RawgPagingSource(service, search) }
        ).flow
    }

    fun getGameDetails(gameId : Int) : Flow<GameDetails> {
        return flow {
            val gameDetails = service.getGameDetails(gameId, RAWG_KEY)
            emit(gameDetails)
        }.flowOn(Dispatchers.IO)
    }

     fun getGameDetailsScreenshots(gameId: Int) : Flow<List<Screenshots>?> {
         return flow {
             val screenshotsResponse = service.getGameDetailsScreenshots(gameId, RAWG_KEY).results
             emit(screenshotsResponse)
         }.flowOn(Dispatchers.IO)
    }
}