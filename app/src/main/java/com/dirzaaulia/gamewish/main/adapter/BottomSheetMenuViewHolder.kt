package com.dirzaaulia.gamewish.main.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dirzaaulia.gamewish.databinding.ItemMenuMainBinding
import com.dirzaaulia.gamewish.main.model.MainMenuModelItem

sealed class BottomSheetMenuViewHolder<T : MainMenuModelItem>(
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(menuItem: T)

    class BottomSheetMenuItemViewHolder(
            private val binding: ItemMenuMainBinding,
            private val listener: BottomSheetMenuAdapter.BottomSheetMenuAdapterListener
    ) : BottomSheetMenuViewHolder<MainMenuModelItem.MainMenuItem>(binding.root) {

        override fun bind(menuItem: MainMenuModelItem.MainMenuItem) {
            binding.run {
                mainMenuItem = menuItem
                mainMenuListener = listener
                executePendingBindings()
            }
        }
    }
}