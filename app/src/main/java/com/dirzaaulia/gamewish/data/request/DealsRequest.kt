package com.dirzaaulia.gamewish.data.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DealsRequest(
    val storeID: String?,
    val lowerPrice: Int?,
    val upperPrice: Int?,
    val title: String?,
    val AAA: Boolean?
) : Parcelable