package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.rawg.*
import com.dirzaaulia.gamewish.network.rawg.pagingsource.SearchGamesPagingSource
import com.dirzaaulia.gamewish.network.rawg.pagingsource.PublishersPagingSource
import com.dirzaaulia.gamewish.network.rawg.RawgService
import com.dirzaaulia.gamewish.network.rawg.pagingsource.GenresPagingSource
import com.dirzaaulia.gamewish.network.rawg.pagingsource.PlatformsPagingSource
import com.dirzaaulia.gamewish.util.GAMES_PAGE_SIZE
import com.dirzaaulia.gamewish.util.RAWG_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RawgRepository @Inject constructor(private val service: RawgService) {

    fun refreshSearchGames(search: String?, genres: Int?, publisher: Int?, platforms: Int?)
    : Flow<PagingData<Games>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { SearchGamesPagingSource(service, search, genres, publisher, platforms) }
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

    fun getGenres() : Flow<PagingData<Genre>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { GenresPagingSource(service) }
        ).flow
    }

    fun getPublishers() : Flow<PagingData<Publisher>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { PublishersPagingSource(service) }
        ).flow
    }

    fun getPlatforms() : Flow<PagingData<Platform>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = GAMES_PAGE_SIZE),
            pagingSourceFactory = { PlatformsPagingSource(service) }
        ).flow
    }
}