package com.dirzaaulia.gamewish.data.response

import com.dirzaaulia.gamewish.data.models.rawg.Publisher

data class PublishersResponse (
    val count : Int?,
    val next : String?,
    val previous : String?,
    val results : List<Publisher>?
)