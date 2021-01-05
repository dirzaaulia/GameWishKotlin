package com.dirzaaulia.gamewish.network.rawg

import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface RawgService {

    @GET("games")
    suspend fun searchGames(
        @Query("key") key: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("search_exact") searchExact: Boolean,
        @Query("search") search: String
    ) : SearchGamesResponse

    companion object {
        private const val BASE_URL = "https://api.rawg.io/api/"

        fun create(): RawgService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BODY }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RawgService::class.java)
        }
    }
}