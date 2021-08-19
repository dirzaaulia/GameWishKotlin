package com.dirzaaulia.gamewish.modules.fragment.search.modules.anime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.gamewish.repository.ProtoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchAnimeViewModel @Inject constructor(
    protoRepository: ProtoRepository
) : ViewModel() {

    private val userPreferencesFlow = protoRepository.userPreferencesFlow

    private val _tabPosition = MutableLiveData<Int>()
    val tabPostion : LiveData<Int>
        get() = _tabPosition

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery : LiveData<String>
        get() = _searchQuery

    private val _accessToken = MutableLiveData<String>()
    val accessToken : LiveData<String>
        get() = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken : LiveData<String>
        get() = _refreshToken

    fun setTabPosition(tabPosition : Int) {
        _tabPosition.value = tabPosition
    }

    fun setSearchQuery(query : String) {
        _searchQuery.value = query
    }

    init {
        getSavedMyAnimeListToken()
        setSearchQuery("")
    }

    private fun getSavedMyAnimeListToken() {
        viewModelScope.launch {
            userPreferencesFlow.collect {
                Timber.i("accessToken : %s\nrefreshToken : %s\nexpiresIn : %d", it
                    .accessToken, it.refreshToken, it.expiresIn)
                _accessToken.value = it.accessToken
                _refreshToken.value = it.refreshToken
            }
        }
    }
}
