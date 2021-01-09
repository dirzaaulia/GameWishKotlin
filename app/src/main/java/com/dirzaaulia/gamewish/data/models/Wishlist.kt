package com.dirzaaulia.gamewish.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_table")
data class Wishlist(
    @PrimaryKey
    val id : Int?,
    val name : String?,
    val imgage : String?,
)