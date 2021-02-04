package com.dirzaaulia.gamewish.modules.deals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.Deals
import com.dirzaaulia.gamewish.data.request.DealsRequest
import com.dirzaaulia.gamewish.data.models.Stores
import com.dirzaaulia.gamewish.repository.CheapSharkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: CheapSharkRepository
) : ViewModel() {

    private var _currentDealsResult: Flow<PagingData<Deals>>? = null

    private var _storeList = MutableLiveData<List<Stores>>()
    val storeList: LiveData<List<Stores>>
        get() = _storeList

    private val _storeName = MutableLiveData<String>()
    val storeName : LiveData<String>
        get() = _storeName

    fun refreshDeals(request: DealsRequest): Flow<PagingData<Deals>> {
        val newResult: Flow<PagingData<Deals>> =
            repository.refreshDeals(request).cachedIn(viewModelScope)

        _currentDealsResult = newResult

        return newResult
    }

    fun getStoreList() {
        viewModelScope.launch {
            try {
                _storeList.value = repository.getAllStores()
            } catch (e : Exception) {
                e.printStackTrace()
            }

        }
    }

    fun updateStoreList(storeName : String){
        _storeName.value = storeName

    }
}