package com.dirzaaulia.gamewish.modules.fragment.home

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.data.models.myanimelist.ListStatus
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.data.response.myanimelist.MyAnimeListUpdateListResponse
import com.dirzaaulia.gamewish.repository.FirebaseRepository
import com.dirzaaulia.gamewish.repository.MyAnimeListRepository
import com.dirzaaulia.gamewish.repository.ProtoRepository
import com.dirzaaulia.gamewish.repository.WishlistRepository
import com.dirzaaulia.gamewish.util.MYANIMELIST_CLIENT_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val wishlistRepository: WishlistRepository,
    private val protoRepository: ProtoRepository,
    private val firebaseRepository: FirebaseRepository,
    private val myAnimeListRepository: MyAnimeListRepository
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private var currentAnimeSort : String? = null
    private var currentAnimeResult  : Flow<PagingData<ParentNode>>? = null
    private var currentMangaResult : Flow<PagingData<ParentNode>>? = null

    private val _userAuthId = MutableLiveData<String>()
    val userAuthId : LiveData<String>
        get() = _userAuthId

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken : LiveData<String>
        get() = _refreshToken

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String>
        get() = _errorMessage

    private val _updateResponse = MutableLiveData<ListStatus?>()
    val updateResponse : LiveData<ListStatus?>
        get() = _updateResponse

    val query = MutableLiveData<String>()

    val listWishlist = query.asFlow()
        .flatMapLatest {
            wishlistRepository.getFilteredWishlist(it)
        }
        .asLiveData()

    init {
        getUserAuthStatus()
        setSearchQuery("")
        _accessToken.value?.let { getMyAnimeListAnimeList(it, null) }
        _accessToken.value?.let { getMyAnimeListMangaList(it, null) }
    }

    fun setSearchQuery(searchQuery : String) {
        query.value = searchQuery
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return firebaseRepository.getFirebaseAuth()
    }

    private fun getUserAuthStatus() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                _userAuthId.value = it.userAuthId.toString()
                _accessToken.value = it.accessToken
                _refreshToken.value = it.refreshToken
            }
        }
    }

    fun setUserAuthId(uid : String) {
        viewModelScope.launch {
            protoRepository.updateUserAuthId(uid)
        }
    }

    fun getAllWishlistFromRealtimeDatabase(uid : String) {
        viewModelScope.launch {
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
    }

    fun addToWishlist(wishlist: Wishlist) {
        viewModelScope.launch {
            wishlistRepository.addToWishlist(wishlist)
        }
    }

    private fun getMyAnimeListRefreshToken(refreshToken : String) {
        viewModelScope.launch {
            try {
                val response = myAnimeListRepository.
                getMyAnimeListRefreshToken(MYANIMELIST_CLIENT_ID, refreshToken)

                response.collect {
                    if (it.accessToken != null) {
                        _accessToken.value = it.accessToken.toString()

                        protoRepository.updateMyAnimeListAccessToken(it.accessToken.toString())
                        protoRepository.updateMyAnimeListRefreshToken(it.refreshToken.toString())
                        protoRepository.updateMyAnimeListExpresIn(it.expiresIn!!)
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                _errorMessage.value = "Something went wrong when getting data from MyAnimeList. " +
                        "Please try it again later!"
            }
        }
    }

    fun getMyAnimeListAnimeList(authorization : String, sort : String?): Flow<PagingData<ParentNode>>? {
//        val lastAnimeResult = currentAnimeResult
//
//        if (sort == currentAnimeSort && currentAnimeResult != null) {
//            return lastAnimeResult
//        }
//
//        currentAnimeSort = sort

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.getMyAnimeListAnimeList(authorization, sort).cachedIn(viewModelScope)

        currentAnimeResult = newResult

        return newResult

    }

    fun getMyAnimeListMangaList(authorization : String, sort : String?): Flow<PagingData<ParentNode>>? {
//        val lastAnimeResult = currentMangaResult
//
//        if (sort == currentAnimeSort && currentMangaResult != null) {
//            return lastAnimeResult
//        }
//
//        currentAnimeSort = sort

        val newResult : Flow<PagingData<ParentNode>> =
            myAnimeListRepository.getMyAnimeListMangaList(authorization, sort).cachedIn(viewModelScope)

        currentMangaResult = newResult

        return newResult
    }

    fun updateMyAnimeListAnimeList(
        authorization: String, animeId : Int, status : String, isRewatching : Boolean?, score : Int?, episode : Int?) {
        viewModelScope.launch {
            try {
                val response =  myAnimeListRepository.updateMyAnimeListAnimeList(
                    authorization, animeId, status, isRewatching, score, episode)
                response.collect {
                    if (it.status != null) {
                        _updateResponse.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun deleteMyAnimeListAnimeList(authorization: String, animeId : Int) {
        viewModelScope.launch {
            try {
                myAnimeListRepository.deleteMyAnimeListAnimeList(authorization, animeId)
                _updateResponse.value = null
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun updateMyAnimeListMangaList(
        authorization: String, mangaId : Int, status : String, isRewatching : Boolean?, score : Int?, episode : Int?) {
        viewModelScope.launch {
            try {
                val response =  myAnimeListRepository.updateMyAnimeListMangaList(
                    authorization, mangaId, status, isRewatching, score, episode)
                response.collect {
                    if (it.status != null) {
                        _updateResponse.value = it
                    }
                }
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }

    fun deleteMyAnimeListMangaList(authorization: String, mangaId : Int) {
        viewModelScope.launch {
            try {
                myAnimeListRepository.deleteMyAnimeListMangaList(authorization, mangaId)
                _updateResponse.value = null
            } catch (e : Exception) {
                e.printStackTrace()
                val errorMessage = e.message.toString()

                if (errorMessage.contains("HTTP 401", true)) {
                    Timber.i("refreshToken")
                    _refreshToken.value?.let { getMyAnimeListRefreshToken(it) }
                } else {
                    _errorMessage.value = "Something went wrong when getting data from MyAnimeList." +
                            " Please try it again later!"
                }
            }
        }
    }
}