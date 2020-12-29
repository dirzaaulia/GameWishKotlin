package com.dirzaaulia.gamewish.modules.main.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil

sealed class MainMenuModelItem {

    data class MainMenuItem(
        val id: Int,
        @DrawableRes val imageIcon: Int,
        @StringRes val titleRes: Int,
        var checked: Boolean,
        val directions: NavDirections
    ) : MainMenuModelItem()

    object MainMenuModelItemDiff : DiffUtil.ItemCallback<MainMenuModelItem>() {
        override fun areItemsTheSame(oldItem: MainMenuModelItem, newItem: MainMenuModelItem): Boolean {
            return when {
                oldItem is MainMenuItem && newItem is MainMenuItem ->
                    oldItem.id == newItem.id
                else -> oldItem == newItem
            }
        }

        override fun areContentsTheSame(oldItem: MainMenuModelItem, newItem: MainMenuModelItem): Boolean {
            return when {
                oldItem is MainMenuItem && newItem is MainMenuItem ->
                    oldItem.imageIcon == newItem.imageIcon &&
                            oldItem.titleRes == newItem.titleRes &&
                            oldItem.checked == newItem.checked
                else -> false
            }
        }
    }
}