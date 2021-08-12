package com.dirzaaulia.gamewish.data.models.rawg

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "wishlist_table")
@Parcelize
data class Wishlist(
    @PrimaryKey
    val id : Int?,
    val name : String?,
    val image : String?,
) : Parcelable