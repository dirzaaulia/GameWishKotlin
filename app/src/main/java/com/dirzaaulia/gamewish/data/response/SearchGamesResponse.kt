package com.dirzaaulia.gamewish.data.response

import com.dirzaaulia.gamewish.data.models.Games

data class SearchGamesResponse(
    val count: Int,
    val results: List<Games>
)