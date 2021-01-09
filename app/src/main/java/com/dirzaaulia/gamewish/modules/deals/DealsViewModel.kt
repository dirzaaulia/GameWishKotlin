package com.dirzaaulia.gamewish.modules.deals

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.Deals
import com.dirzaaulia.gamewish.data.models.DealsRequest
import com.dirzaaulia.gamewish.data.models.Stores
import com.dirzaaulia.gamewish.repository.CheapSharkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DealsViewModel @ViewModelInject constructor(
    private val repository: CheapSharkRepository
) : ViewModel() {

    private var currentDealsResult: Flow<PagingData<Deals>>? = null

    private var _storeList = MutableLiveData<List<Stores>>()
    val storeList: MutableLiveData<List<Stores>>
        get() = _storeList

    fun refreshDeals(request: DealsRequest): Flow<PagingData<Deals>> {
        val newResult: Flow<PagingData<Deals>> =
            repository.refreshDeals(request).cachedIn(viewModelScope)

        currentDealsResult = newResult

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
}