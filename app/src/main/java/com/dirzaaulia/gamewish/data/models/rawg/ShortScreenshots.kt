package com.dirzaaulia.gamewish.data.models.rawg

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShortScreenshots(
    val id: Int?,
    val image: String?
) : Parcelable