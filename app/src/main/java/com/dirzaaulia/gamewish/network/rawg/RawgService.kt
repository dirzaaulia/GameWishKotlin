package com.dirzaaulia.gamewish.network.rawg

import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.response.ScreenshotsResponse
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.util.RAWG_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Provides
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface RawgService {

    @GET("games")
    suspend fun searchGames(
        @Query("key") key: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("search_precise") searchPrecise: Boolean,
        @Query("search") search: String
    ) : SearchGamesResponse

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") id: Int,
        @Query("key") key: String
    ) : GameDetails

    @GET("games/{id}/screenshots")
    suspend fun getGameDetailsScreenshots(
        @Path("id") id : Int,
        @Query("key") key : String
    ) : ScreenshotsResponse

    companion object {
        fun create(): RawgService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BODY }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(RAWG_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RawgService::class.java)
        }
    }
}