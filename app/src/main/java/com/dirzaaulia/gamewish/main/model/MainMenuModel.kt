package com.dirzaaulia.gamewish.main.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dirzaaulia.gamewish.R

object MainMenuModel {

    private var mainMenuItems = mutableListOf(
            MainMenuModelItem.MainMenuItem(
                    id = 0,
                    imageIcon = R.drawable.ic_baseline_add_24,
                    title = "Free Games",
                    checked = false
            ),
            MainMenuModelItem.MainMenuItem(
                    id = 1,
                    imageIcon = R.drawable.ic_baseline_add_24,
                    title = "Free Games 2",
                    checked = false
            )
    )

    private val _mainMenuList: MutableLiveData<List<MainMenuModelItem>> =  MutableLiveData()
        val mainMenuList: LiveData<List<MainMenuModelItem>>
            get() = _mainMenuList

    fun setMainMenuItemChecked(id: Int): Boolean {
        var updated = false

        mainMenuItems.forEachIndexed { index, mainMenuItem ->
            val shouldCheck = mainMenuItem.id == id
            if (mainMenuItem.checked != shouldCheck) {
                mainMenuItems[index] = mainMenuItem.copy(checked = shouldCheck)
                updated = true
            }
        }

        if (updated) postListUpdate()
        return updated
    }

    private fun postListUpdate() {
        val newList = mainMenuItems

        _mainMenuList.value = newList
    }
}