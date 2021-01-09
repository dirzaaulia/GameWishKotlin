package com.dirzaaulia.gamewish.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Games(
    val id: Int?,
    val name: String?,
    val released: String?,
    val metacritic: Int?,
    val platforms: List<Platforms>,
    @Json(name = "background_image")
    val backgroundImage: String?,
    @Json(name = "esrb_rating")
    val esrbRating: EsrbRating?,
    @Json(name = "short_screenshots")
    val shortScreenshots: List<ShortScreenshots>?
) : Parcelable