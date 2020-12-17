package com.dirzaaulia.gamewish.main.model

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.home.HomeFragmentDirections

object MainMenuModel {

    const val HOME_ID = 0
    const val DEALS_ID = 1

    private var mainMenuItems = mutableListOf(
            MainMenuModelItem.MainMenuItem(
                    id = HOME_ID,
                    imageIcon = R.drawable.ic_baseline_add_24,
                    titleRes = R.string.app_name,
                    checked = false,
                directions = HomeFragmentDirections.actionGlobalHomeFragment()
            ),
            MainMenuModelItem.MainMenuItem(
                    id = DEALS_ID,
                    imageIcon = R.drawable.ic_baseline_add_24,
                    titleRes = R.string.deals,
                    checked = false,
                directions = HomeFragmentDirections.actionGlobalDealsFragment()
            )
    )

    private val _mainMenuList: MutableLiveData<List<MainMenuModelItem>> =  MutableLiveData()
        val mainMenuList: LiveData<List<MainMenuModelItem>>
            get() = _mainMenuList

    init {
        postListUpdate()
    }

    fun setMainMenuItemChecked(id: Int) : Boolean {
        var updated = false
        mainMenuItems.forEachIndexed { index, mainMenuItem ->
            val shouldCheck = mainMenuItem.id == id
            if (mainMenuItem.checked != shouldCheck) {
                mainMenuItems[index] = mainMenuItem.copy(checked = shouldCheck)
                updated = true
            }
            Log.i("MainMenuModel", mainMenuItem.id.toString() + " " + mainMenuItem.checked.toString())
        }

        if (updated) postListUpdate()
        return updated
    }

    private fun postListUpdate() {
        val newList = mainMenuItems

        _mainMenuList.value = newList
    }
}