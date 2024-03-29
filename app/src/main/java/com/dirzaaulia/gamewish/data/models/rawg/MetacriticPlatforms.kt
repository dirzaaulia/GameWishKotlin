package com.dirzaaulia.gamewish.data.models.rawg

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetacriticPlatforms(
    val metascore : Int?,
    val url : String?,
    val platform: Platform?
) : Parcelable