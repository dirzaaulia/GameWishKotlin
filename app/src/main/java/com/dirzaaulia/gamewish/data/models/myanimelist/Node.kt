package com.dirzaaulia.gamewish.data.models.myanimelist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Entity(tableName = "myanimelist_table")
@Parcelize
data class Node (
    @PrimaryKey
    val id : Int?,
    val title : String?,
    @Json(name = "main_picture")
    val mainPicture: MainPicture?,
    @Ignore
    @Json(name = "first_name")
    val firstName : String?,
    @Ignore
    @Json(name = "last_name")
    val lastName : String?
) : Parcelable