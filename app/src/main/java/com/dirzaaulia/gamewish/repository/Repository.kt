package com.dirzaaulia.gamewish.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dirzaaulia.gamewish.models.Deals
import com.dirzaaulia.gamewish.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class Repository() {

    suspend fun refreshDeals() : List<Deals> {
        return withContext(Dispatchers.IO) {
            Network.cheapShark.getGameDeals()
        }
    }
}