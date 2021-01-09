package com.dirzaaulia.gamewish.modules.details

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.*
import com.dirzaaulia.gamewish.repository.RawgRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class DetailsViewModel @AssistedInject constructor(
    private val wishlistRepository: WishlistRepository,
    private val rawgRepository: RawgRepository,
    @Assisted private val games: Games
) : ViewModel() {

    private val _wishlist = MutableLiveData<Wishlist>()
    val wishlist: LiveData<Wishlist>
        get() = _wishlist

    private var _gameDetails = MutableLiveData<GameDetails>()
    val gameDetails: LiveData<GameDetails>
        get() = _gameDetails

    private var _gameDetailsScreenshots = MutableLiveData<List<Screenshots>>()
    val gameDetailsScreenshots: LiveData<List<Screenshots>>
        get() = _gameDetailsScreenshots

    private var _developer = MutableLiveData<Developer>()
    val developer: LiveData<Developer>
        get() = _developer

    private var _publisher = MutableLiveData<Publisher>()
    val publisher: LiveData<Publisher>
        get() = _publisher

    init {
        getGameDetails(games.id!!)
        getGameDetailsScreenshots(games.id)
        getWishlist(games.id)
    }

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.addToWishlist(wishlist)
        }
    }

    private fun getWishlist(gameId: Int) {
        viewModelScope.launch {
           _wishlist.value = wishlistRepository.getWistlist(gameId)
        }
    }

    private fun getGameDetails(gameId : Int) {
        viewModelScope.launch {
            try {
                _gameDetails.value = rawgRepository.getGameDetails(gameId)

                if (_gameDetails.value!!.developers?.isNotEmpty() == true) {
                    _developer.value = _gameDetails.value!!.developers?.get(0)
                }

                if (_gameDetails.value!!.publishers?.isNotEmpty() == true) {
                    _publisher.value = _gameDetails.value!!.publishers?.get(0)
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun getGameDetailsScreenshots(gameId : Int) {
        viewModelScope.launch {
            try {
                _gameDetailsScreenshots.value = rawgRepository.getGameDetailsScreenshots(gameId).results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(games: Games): DetailsViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            games: Games
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(games) as T
            }
        }
    }
}