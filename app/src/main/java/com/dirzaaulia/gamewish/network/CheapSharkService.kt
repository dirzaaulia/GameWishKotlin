package com.dirzaaulia.gamewish.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dirzaaulia.gamewish.models.Deals
import com.dirzaaulia.gamewish.models.Stores
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.log


interface CheapSharkService {
    @GET("deals")
    suspend fun getGameDeals(
        @Query("storeID") storeID: String,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
        @Query("lowerPrice") lowerPrice: Int,
        @Query("upperPrice") upperPrice: Int,
        @Query("title") title: String,
        @Query("AAA") AAA: Boolean
    ): List<Deals>

    @GET("stores")
    suspend fun getStoresList() : List<Stores>

    companion object {
        private const val BASE_URL = "https://www.cheapshark.com/api/1.0/"

        fun create(): CheapSharkService {
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
                    .create(CheapSharkService::class.java)
        }

    }
}

