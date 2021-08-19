package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.myanimelist.*
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListUpdateListResponse
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListBaseUrlService
import com.dirzaaulia.gamewish.network.myanimelist.pagingsource.SearchMyAnimeListPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject

class MyAnimeListRepository @Inject constructor(
    private val baseUrlService : MyAnimeListBaseUrlService,
    private val apiUrlService : MyAnimeListApiUrlService
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
            pagingSourceFactory = {
                SearchMyAnimeListPagingSource(
                    apiUrlService, 1, authorization, query, null, null, null) }
        ).flow
    }

    fun searchMyAnimeListManga(accessToken: String, query : String) : Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = {
                SearchMyAnimeListPagingSource(
                    apiUrlService, 2, authorization, query, null, null, null) }
        ).flow
    }

    fun getMyAnimeListSeasonal(accessToken: String, year : String, season : String) : Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = {
                SearchMyAnimeListPagingSource(
                    apiUrlService, 3, authorization, null, year, season, null) }
        ).flow
    }

    fun getMyAnimeListAnimeDetails(accessToken: String, animeId : String) : Flow<Details> {
        return flow {
            val authorization = String.format("Bearer %s", accessToken)
            val fields = "id,title,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity," +
                    "num_list_users,media_type,status,genres,my_list_status,num_episodes,source,rating,pictures," +
                    "related_anime,related_manga,recommendations"
            val response = apiUrlService.getMyAnimeListAnimeDetails(authorization, animeId, fields, 0)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getMyAnimeListMangaDetails(accessToken: String, animeId : String) : Flow<Details> {
        return flow {
            val authorization = String.format("Bearer %s", accessToken)
            val fields = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank," +
                    "popularity,num_list_users,media_type,status,genres,my_list_status,num_chapters," +
                    "authors{first_name,last_name},pictures,background,related_anime,related_manga,recommendations"
            val response = apiUrlService.getMyAnimeListMangaDetails(authorization, animeId, fields, 0)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getMyAnimeListAnimeList(accessToken: String, sort : String?): Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = {
                SearchMyAnimeListPagingSource(
                    apiUrlService, 4, authorization, null, null, null, sort) }
        ).flow
    }

    fun getMyAnimeListMangaList(accessToken: String, sort : String?): Flow<PagingData<ParentNode>> {
        val authorization = String.format("Bearer %s", accessToken)
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 10),
            pagingSourceFactory = {
                SearchMyAnimeListPagingSource(
                    apiUrlService, 5, authorization, null, null, null, sort) }
        ).flow
    }

    fun updateMyAnimeListAnimeList(
        accessToken : String, animeId : Int, status : String, isRewatching : Boolean?, score : Int?, episode : Int?)
            : Flow<ListStatus> {
        return flow {
            val authorization = String.format("Bearer $accessToken")
            val response = apiUrlService.updateMyAnimeListAnimeList(
                authorization, animeId, status, isRewatching, score, episode)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteMyAnimeListAnimeList(accessToken: String, animeId: Int) {
        val authorization = String.format("Bearer $accessToken")
        apiUrlService.deleteMyAnimeListAnimeList(authorization, animeId)
    }

    fun updateMyAnimeListMangaList(
        accessToken : String, mangaId : Int, status : String, isReReading : Boolean?, score : Int?, episode : Int?)
            : Flow<ListStatus> {
        return flow {
            val authorization = String.format("Bearer $accessToken")
            val response = apiUrlService.updateMyAnimeListMangaList(
                authorization, mangaId, status, isReReading, score, episode)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteMyAnimeListMangaList(accessToken: String, mangaId: Int) {
        val authorization = String.format("Bearer $accessToken")
        apiUrlService.deleteMyAnimeListMangaList(authorization, mangaId)
    }
}