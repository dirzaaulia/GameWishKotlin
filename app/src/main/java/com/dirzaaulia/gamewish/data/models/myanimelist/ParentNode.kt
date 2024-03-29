package com.dirzaaulia.gamewish.data.models.myanimelist

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParentNode (
    val node : Node?,
    @Json(name = "relation_type_formatted")
    val relationType : String?,
    @Json(name = "list_status")
    var listStatus : ListStatus?,
    val role : String?
) : Parcelable