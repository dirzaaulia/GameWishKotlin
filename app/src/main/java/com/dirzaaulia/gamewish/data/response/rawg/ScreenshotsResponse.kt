package com.dirzaaulia.gamewish.data.response.rawg

import com.dirzaaulia.gamewish.data.models.rawg.Screenshots

data class ScreenshotsResponse (
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Screenshots>?
)