package com.dirzaaulia.gamewish.modules.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.dirzaaulia.gamewish.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    //    private val filterChannel = ConflatedBroadcastChannel<String>()
    val query = MutableLiveData<String>()

//    val listWishlist = filterChannel.asFlow()
//        .flatMapLatest {
//            wishlistRepository.getFilteredWishlist(it)
//        }
//        .asLiveData()

    val listWishlist = query.asFlow()
        .flatMapLatest {
            wishlistRepository.getFilteredWishlist(it)
        }
        .asLiveData()


//    fun setFilterQuery(gameName : String) {
//        filterChannel.offer(gameName)
//    }
}