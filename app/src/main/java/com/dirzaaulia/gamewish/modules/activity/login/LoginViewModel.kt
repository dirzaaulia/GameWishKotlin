package com.dirzaaulia.gamewish.modules.activity.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.FirebaseRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val firebaseRepository: FirebaseRepository,
    private val protoRepository: ProtoRepository
) : ViewModel() {

    private val _googleLoginStatus = MutableLiveData<Boolean>()
    val googleLoginStatus : LiveData<Boolean>
        get() = _googleLoginStatus

    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseRepository.getFirebaseAuth()
    }

    fun authGoogleLogin(idToken: String): AuthCredential {
        return firebaseRepository.authGoogleLogin(idToken)
    }

    fun getGoogleLoginStatus() {
        val status = firebaseRepository.getGoogleLoginStatus()
        _googleLoginStatus.value = status
    }

    fun setLocalDataStatus(status : Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            protoRepository.updateLocalDataStatus(status)
        }
    }
}