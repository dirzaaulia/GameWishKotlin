package com.dirzaaulia.gamewish.models

data class DealsRequest(
    val storeID: String,
    val lowerPrice: Int,
    val upperPrice: Int,
    val title: String,
    val AAA: Boolean
)