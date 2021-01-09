package com.dirzaaulia.gamewish.data.response

import com.dirzaaulia.gamewish.data.models.Screenshots

data class ScreenshotsResponse (
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Screenshots>?
)