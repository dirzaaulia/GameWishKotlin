package com.dirzaaulia.gamewish.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Platform(
    val id: Int?,
    val platform : Int?,
    val name: String?,
    val slug: String?
) : Parcelable