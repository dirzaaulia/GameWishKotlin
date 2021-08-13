package com.dirzaaulia.gamewish.modules.details

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.rawg.GameDetails
import com.dirzaaulia.gamewish.data.models.rawg.Screenshots
import com.dirzaaulia.gamewish.data.models.rawg.Wishlist
import com.dirzaaulia.gamewish.repository.RawgRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DetailsViewModel @AssistedInject constructor(
    private val wishlistRepository: WishlistRepository,
    private val rawgRepository: RawgRepository,
    @Assisted private val gameId: Int
) : ViewModel() {

    val itemWishlist = wishlistRepository.getWishlist(gameId).asLiveData()

    private val _gameDetails = MutableLiveData<GameDetails?>()
    val gameDetails : LiveData<GameDetails?>
        get() = _gameDetails

    private val _gameDetailsScreenshots = MutableLiveData<List<Screenshots>?>()
    val gameDetailsScreenshots : LiveData<List<Screenshots>?>
        get() = _gameDetailsScreenshots

    fun fetchGameDetails() {
        viewModelScope.launch {
            try {
                rawgRepository.getGameDetails(gameId).collect {
                    _gameDetails.value = it
                }
            } catch (e : Exception) {
                e.printStackTrace()
                _gameDetails.value = null
            }
        }
    }

    fun fetchGameDetailsScrenshoots() {
        viewModelScope.launch {
            try {
                rawgRepository.getGameDetailsScreenshots(gameId).collect {
                    _gameDetailsScreenshots.value = it
                }
            } catch (e : Exception) {
                e.printStackTrace()
                _gameDetailsScreenshots.value = emptyList()
            }
        }
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