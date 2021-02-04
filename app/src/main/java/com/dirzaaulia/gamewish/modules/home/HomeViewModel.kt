package com.dirzaaulia.gamewish.modules.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val filterChannel = ConflatedBroadcastChannel<String>()

    val listWishlist = filterChannel.asFlow()
        .flatMapLatest {
            wishlistRepository.getFilteredWishlist(it)
        }
        .asLiveData()

    fun setFilterQuery(gameName : String) {
        filterChannel.offer(gameName)
    }
}