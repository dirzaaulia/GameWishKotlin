package com.dirzaaulia.gamewish.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "wishlist_table")
@Parcelize
data class Wishlist(
    @PrimaryKey
    var id : Int? = null,
    var name : String? = null,
    val image : String? = null
) : Parcelable