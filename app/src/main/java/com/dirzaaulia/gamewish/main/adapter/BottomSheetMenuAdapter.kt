package com.dirzaaulia.gamewish.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.dirzaaulia.gamewish.databinding.ItemMenuMainBinding
import com.dirzaaulia.gamewish.main.model.MainMenuModelItem
import java.lang.RuntimeException

private const val VIEW_TYPE_MAIN_MENU_ITEM = 4

class BottomSheetMenuAdapter (private val listener: BottomSheetMenuAdapterListener) :
        ListAdapter<MainMenuModelItem, BottomSheetMenuViewHolder<MainMenuModelItem>>
        (MainMenuModelItem.MainMenuModelItemDiff) {

    interface BottomSheetMenuAdapterListener {
        fun onMenuItemClicked(item: MainMenuModelItem.MainMenuItem)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MainMenuModelItem.MainMenuItem -> VIEW_TYPE_MAIN_MENU_ITEM
            else -> throw RuntimeException("Unsupported ItemViewType for obj ${getItem(position)}")
        }
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BottomSheetMenuViewHolder<MainMenuModelItem> {
        return when (viewType) {
            VIEW_TYPE_MAIN_MENU_ITEM -> BottomSheetMenuViewHolder.BottomSheetMenuItemViewHolder (
                ItemMenuMainBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                ),
                listener
            )
            else -> throw RuntimeException("Unsupported view holder type")
        } as BottomSheetMenuViewHolder<MainMenuModelItem>
    }

    override fun onBindViewHolder(
            holder: BottomSheetMenuViewHolder<MainMenuModelItem>,
            position: Int
    ) {
        holder.bind(getItem(position))
    }
}