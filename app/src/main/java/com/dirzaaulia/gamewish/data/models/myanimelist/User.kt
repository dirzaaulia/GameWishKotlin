package com.dirzaaulia.gamewish.data.models.myanimelist

import com.squareup.moshi.Json

data class User (
    val id : String?,
    val name : String?,
    val birthday : String?,
    val location : String?,
    @Json(name = "joined_at")
    val joinedAt : String?,
    val picture : String?,
    @Json(name = "anime_statistics")
    val animeStatistics : AnimeStatistic?
)