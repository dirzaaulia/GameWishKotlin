package com.dirzaaulia.gamewish.modules.fragment.details

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.rawg.GameDetails
import com.dirzaaulia.gamewish.data.models.rawg.Screenshots
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.FirebaseRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
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
    private val firebaseRepository: FirebaseRepository,
    private val protoRepository: ProtoRepository,
    @Assisted private val gameId: Int
) : ViewModel() {

    //    val itemWishlist = wishlistRepository.getWishlist(gameId).asLiveData()
    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    val wishlistItem = wishlistRepository.getWishlist(gameId).asLiveData()

    private val _gameDetails = MutableLiveData<GameDetails?>()
    val gameDetails : LiveData<GameDetails?>
        get() = _gameDetails

    private val _gameDetailsScreenshots = MutableLiveData<List<Screenshots>?>()
    val gameDetailsScreenshots : LiveData<List<Screenshots>?>
        get() = _gameDetailsScreenshots

    private val _localDataStatus = MutableLiveData<Boolean>()
    val localDataStatus : LiveData<Boolean>
        get() = _localDataStatus

    private val _userAuthId = MutableLiveData<String>()
    val userAuthId : LiveData<String>
        get() = _userAuthId

    fun getUserAuthId() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                _userAuthId.value = it.userAuthId
            }
        }
    }

    fun getLocalDataStatus() {
        viewModelScope.launch {
            _localDataStatus.value = firebaseRepository.getGoogleLoginStatus()
        }
    }

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

    fun addToWishlistFirebase(uid : String, wishlist: Wishlist) {
        firebaseRepository.addWishlistToRealtimeDatabase(uid, wishlist)
    }

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.addToWishlist(wishlist)
        }
    }

    fun removeFromWishlistFirebase(uid : String, wishlist: Wishlist) {
        viewModelScope.launch {
            try {
                firebaseRepository.removeWishlistFromRealtimeDatabase(uid, wishlist)
            } catch (e : Exception) {
                e.printStackTrace()
            }
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