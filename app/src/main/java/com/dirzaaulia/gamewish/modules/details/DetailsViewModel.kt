package com.dirzaaulia.gamewish.modules.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.gamewish.data.models.rawg.Wishlist
import com.dirzaaulia.gamewish.repository.RawgRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch


class DetailsViewModel @AssistedInject constructor(
    private val wishlistRepository: WishlistRepository,
    rawgRepository: RawgRepository,
    @Assisted private val gameId: Int
) : ViewModel() {

    val gameDetails = rawgRepository.getGameDetails(gameId).asLiveData()
    val gameDetailsScreenshots = rawgRepository.getGameDetailsScreenshots(gameId).asLiveData()
    val itemWishlist = wishlistRepository.getWishlist(gameId).asLiveData()

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.addToWishlist(wishlist)
        }
    }

    fun removeFromWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(wishlist)
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: DetailsViewModelFactory,
            gameId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(gameId) as T
            }
        }
    }
}

@AssistedFactory
interface DetailsViewModelFactory {
    fun create(gameId: Int): DetailsViewModel
}