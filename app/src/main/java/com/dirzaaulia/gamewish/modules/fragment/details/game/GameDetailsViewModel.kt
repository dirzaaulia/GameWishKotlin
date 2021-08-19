package com.dirzaaulia.gamewish.modules.fragment.details.game

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.rawg.GameDetails
import com.dirzaaulia.gamewish.data.models.rawg.Screenshots
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.FirebaseRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.repository.RawgRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailsViewModel @AssistedInject constructor(
    private val wishlistRepository: WishlistRepository,
    private val rawgRepository: RawgRepository,
    private val firebaseRepository: FirebaseRepository,
    private val protoRepository: ProtoRepository,
    @Assisted private val gameId: Int
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    val wishlistItem = wishlistRepository.getWishlist(gameId).asLiveData()

    private lateinit var _wishlistItemFirebase : LiveData<Wishlist>
    val wishlistItemFirebase : LiveData<Wishlist>
        get() = _wishlistItemFirebase

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

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage

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

    fun getWishlistFromRealtimeDatabase(uid: String, gameId: String) {
        try {
            val task = firebaseRepository.getWishlistFromRealtimeDatabase(uid, gameId)

            task.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   val value = snapshot.getValue(Wishlist::class.java)

                    if (value != null) {
                        val valueFlow = flow {
                            emit(value)
                        }

                        _wishlistItemFirebase = valueFlow.asLiveData()
                        Timber.i(_wishlistItemFirebase.value?.name)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        } catch (e : java.lang.Exception) {

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
                _errorMessage.value = "Something when wrong when getting game data. " +
                        "Please try it again!"
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