package com.dirzaaulia.gamewish.main.model

import androidx.annotation.DrawableRes

sealed class MainMenuModelItem {

    data class MainMenuItem(
            val id: Int,
            val imageIcon: Int,
            val title: String,
            val checked: Boolean
    ) : MainMenuModelItem()
}