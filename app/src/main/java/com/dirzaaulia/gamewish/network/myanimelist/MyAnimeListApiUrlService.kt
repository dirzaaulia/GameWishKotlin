package com.dirzaaulia.gamewish.network.myanimelist

import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.util.MYANIMELIST_API_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface MyAnimeListApiUrlService {

    @GET("v2/users/{user_name}")
    suspend fun getMyAnimeListUsername(
        @Header("Authorization") authorization : String,
        @Path("user_name") username : String,
        @Query("fields") fields : String
    ) : User

    companion object {
        fun create(): MyAnimeListApiUrlService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

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
                .baseUrl(MYANIMELIST_API_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MyAnimeListApiUrlService::class.java)
        }
    }
}