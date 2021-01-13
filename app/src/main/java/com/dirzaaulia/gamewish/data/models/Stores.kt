package com.dirzaaulia.gamewish.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stores(
        val storeID: String?,
        val storeName: String?,
        val id: Int?,
        val url: String?,
        val store: Store?
) : Parcelable