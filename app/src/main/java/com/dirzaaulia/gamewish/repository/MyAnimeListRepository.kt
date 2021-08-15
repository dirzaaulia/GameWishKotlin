package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListSearchResponse
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListBaseUrlService
import com.dirzaaulia.gamewish.network.myanimelist.pagingsource.SearchAnimePagingSource
import com.dirzaaulia.gamewish.network.myanimelist.pagingsource.SearchMangaPagingSource
import com.dirzaaulia.gamewish.network.rawg.pagingsource.SearchGamesPagingSource
import com.dirzaaulia.gamewish.util.GAMES_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MyAnimeListRepository @Inject constructor(
    private val baseUrlService : MyAnimeListBaseUrlService,
    private val apiUrlService: MyAnimeListApiUrlService
) {

    fun getMyAnimeListToken(clientId : String, code : String, codeVerifier : String, grantType : String) :
            Flow<MyAnimeListTokenResponse> {
        return flow {
            val response = baseUrlService.getMyAnimeListToken(clientId, code, codeVerifier, grantType)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getMyAnimeListRefreshToken(clientId : String, refreshToken : String) : Flow<MyAnimeListTokenResponse> {
        return flow {
            val response = baseUrlService.
            getMyAnimeListRefreshToken(clientId, "refresh_token", refreshToken)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getMyAnimeListUser(accessToken : String) : Flow<User> {
        return flow {
            val authorization = String.format("Bearer %s", accessToken)
            val response = apiUrlService.getMyAnimeListUser(authorization,
                "@me", "anime_statistics")
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun searchMyAnimeListAnime(accessToken: String, query : String) : Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { SearchAnimePagingSource(apiUrlService, authorization, query) }
        ).flow
    }

    fun searchMyAnimeListManga(accessToken: String, query : String) : Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = { SearchMangaPagingSource(apiUrlService, authorization, query) }
        ).flow
    }
}