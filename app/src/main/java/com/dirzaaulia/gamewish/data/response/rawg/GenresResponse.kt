package com.dirzaaulia.gamewish.data.response.rawg

import com.dirzaaulia.gamewish.data.models.rawg.Genre

data class GenresResponse (
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Genre>?
)