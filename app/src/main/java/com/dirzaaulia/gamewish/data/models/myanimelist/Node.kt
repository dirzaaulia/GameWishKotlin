package com.dirzaaulia.gamewish.data.models.myanimelist

import com.squareup.moshi.Json

data class Node (
    val id : Int?,
    val title : String?,
    @Json(name = "main_picture")
    val mainPicture: MainPicture?
)