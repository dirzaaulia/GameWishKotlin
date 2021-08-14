package com.dirzaaulia.gamewish.data.response.rawg

import com.dirzaaulia.gamewish.data.models.rawg.Games

data class SearchGamesResponse(
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Games>?
)