package com.dirzaaulia.gamewish.data.models.myanimelist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Genre (
    val id : Int?,
    val name : String
) : Parcelable