package com.dirzaaulia.gamewish.repository

import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListBaseUrlService
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

    fun getMyAnimeListUsername(accessToken : String) : Flow<User> {
        return flow {
            val authorization = String.format("Bearer %s", accessToken)
            val response = apiUrlService.getMyAnimeListUsername( authorization,"@me", "anime_statistics")
            emit(response)
        }.flowOn(Dispatchers.IO)
    }
}