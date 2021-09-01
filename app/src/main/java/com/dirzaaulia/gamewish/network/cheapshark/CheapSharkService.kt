package com.dirzaaulia.gamewish.network.cheapshark

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.models.rawg.Stores
import com.dirzaaulia.gamewish.util.CHEAPSHARK_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface CheapSharkService {
    @GET("deals")
    suspend fun getGameDeals(
        @Query("storeID") storeID: String?,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
        @Query("lowerPrice") lowerPrice: Int?,
        @Query("upperPrice") upperPrice: Int?,
        @Query("title") title: String?,
        @Query("AAA") AAA: Boolean?
    ): List<Deals>

    @GET("stores")
    suspend fun getStoresList() : List<Stores>

    companion object {
        fun create(context: Context): CheapSharkService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BODY }
            val chuckerLogger = ChuckerInterceptor.Builder(context)
                .collector(ChuckerCollector(context))
                .maxContentLength(250000L)
                .redactHeaders(emptySet())
                .alwaysReadResponseBody(false)
                .build()

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(chuckerLogger)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(CHEAPSHARK_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CheapSharkService::class.java)
        }
    }
}

