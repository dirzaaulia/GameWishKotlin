package com.dirzaaulia.gamewish.models

import com.squareup.moshi.JsonClass


data class Deals(
    val internalName: String,
    val title: String,
    val storeID: String,
    val gameID: String,
    val salePrice: String,
    val normalPrice: String,
    val releaseDate: Long,
    val steamAppID: String?,
    val thumb: String
)