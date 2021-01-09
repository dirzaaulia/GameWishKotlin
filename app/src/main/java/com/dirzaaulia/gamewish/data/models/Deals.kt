package com.dirzaaulia.gamewish.data.models

import com.squareup.moshi.JsonClass
import java.lang.reflect.Type

data class Deals(
        val internalName: String?,
        val title: String?,
        val dealID: String?,
        val storeID: String?,
        var storeName: String?,
        val gameID: String?,
        val salePrice: String?,
        val normalPrice: String?,
        val releaseDate: Long?,
        val steamAppID: String?,
        val thumb: String?
)