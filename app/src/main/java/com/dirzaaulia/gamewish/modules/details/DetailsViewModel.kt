package com.dirzaaulia.gamewish.modules.details

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.Wishlist
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

    private val _isWishlisted = MutableLiveData<Boolean>()
    val isWishlisted: LiveData<Boolean>
        get() = _isWishlisted

    init {
        checkIfWishlisted(gameId)
    }

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

    fun checkIfWishlisted(gameId: Int) {
        _isWishlisted.value = wishlistRepository.isWishlisted(gameId).asLiveData().value
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