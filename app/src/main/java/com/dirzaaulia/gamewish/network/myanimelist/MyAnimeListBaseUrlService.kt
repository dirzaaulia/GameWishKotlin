package com.dirzaaulia.gamewish.network.myanimelist

import com.dirzaaulia.gamewish.data.models.myanimelist.User
import com.dirzaaulia.gamewish.data.response.MyAnimeListTokenResponse
import com.dirzaaulia.gamewish.util.MYANIMELIST_API_URL
import com.dirzaaulia.gamewish.util.MYANIMELIST_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface MyAnimeListBaseUrlService {

    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getMyAnimeListToken(
        @Field("client_id") client_id : String,
        @Field("code") code : String,
        @Field("code_verifier") code_verifier : String,
        @Field("grant_type") grant_type : String
    ) : MyAnimeListTokenResponse

    companion object {
        fun create(): MyAnimeListBaseUrlService {
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
                .baseUrl(MYANIMELIST_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MyAnimeListBaseUrlService::class.java)
        }
    }
}