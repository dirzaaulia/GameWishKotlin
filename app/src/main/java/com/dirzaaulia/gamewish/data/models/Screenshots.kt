package com.dirzaaulia.gamewish.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Screenshots(
    val id : Int?,
    val image : String?,
    val width : Int?,
    val height : Int?
) : Parcelable