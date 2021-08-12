package com.dirzaaulia.gamewish.data.response

import com.dirzaaulia.gamewish.data.models.rawg.Games

data class SearchGamesResponse(
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Games>?
)