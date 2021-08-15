package com.dirzaaulia.gamewish.data.response.myanimelist

import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.Paging
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode

data class MyAnimeListSearchResponse (
    val data : List<ParentNode>?,
    val paging : Paging?,
    val error : String?,
    val message : String?
)