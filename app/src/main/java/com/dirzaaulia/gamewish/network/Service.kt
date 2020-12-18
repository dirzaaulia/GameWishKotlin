package com.dirzaaulia.gamewish.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dirzaaulia.gamewish.models.Deals
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val CHEAPSHARK_URL = "https://www.cheapshark.com/api/1.0/"

interface CheapSharkService {
    @GET("deals")
    suspend fun getGameDeals(): List<Deals>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofitCheapShark = Retrofit.Builder()
    .baseUrl(CHEAPSHARK_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

object Network {
    val cheapShark: CheapSharkService by lazy {
        retrofitCheapShark.create(CheapSharkService::class.java)
    }
}

