package com.dirzaaulia.gamewish.modules.fragment.home

import androidx.lifecycle.*
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.FirebaseRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val wishlistRepository: WishlistRepository,
    private val protoRepository: ProtoRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private val _userAuthId = MutableLiveData<String>()
    val userAuthId : LiveData<String>
        get() = _userAuthId

    val query = MutableLiveData<String>()

    val listWishlist = query.asFlow()
        .flatMapLatest {
            wishlistRepository.getFilteredWishlist(it)
        }
        .asLiveData()

    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseRepository.getFirebaseAuth()
    }

    fun getUserAuthId() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                _userAuthId.value = it.userAuthId.toString()
                Timber.i("userAuthId : %s", _userAuthId.value)
            }
        }
    }

    fun setUserAuthId(uid : String) {
        viewModelScope.launch {
            protoRepository.updateUserAuthId(uid)
        }
    }

    fun getAllWishlistFromRealtimeDatabase(uid : String) {
        try {
            val task = firebaseRepository
                .getAllWishlistFromRealtimeDatabase(uid)
            task.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        Timber.i("Wishlist : %s", it.getValue(Wishlist::class.java)?.name)

                        val value = it.getValue(Wishlist::class.java)
                        if (value != null) {
                            addToWishlist(value)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) { }
            })
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.addToWishlist(wishlist)
        }
    }
}