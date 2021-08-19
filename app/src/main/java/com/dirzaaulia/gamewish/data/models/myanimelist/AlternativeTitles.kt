package com.dirzaaulia.gamewish.data.models.myanimelist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AlternativeTitles (
    val en : String?,
    val ja : String?
) : Parcelable