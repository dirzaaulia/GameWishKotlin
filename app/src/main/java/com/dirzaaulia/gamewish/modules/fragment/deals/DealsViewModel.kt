package com.dirzaaulia.gamewish.modules.fragment.deals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.models.rawg.Stores
import com.dirzaaulia.gamewish.data.request.DealsRequest
import com.dirzaaulia.gamewish.repository.CheapSharkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: CheapSharkRepository
) : ViewModel() {

    private var _currentDealsResult: Flow<PagingData<Deals>>? = null
    var currentDealsRequest : DealsRequest = DealsRequest("1", 0, 1000, "", false)

//    private val _currentDealsRequest = MutableLiveData<DealsRequest>()
//    val currentDealsRequest : LiveData<DealsRequest>
//        get() = _currentDealsRequest

    private var _storeList = MutableLiveData<List<Stores>>()
    val storeList: LiveData<List<Stores>>
        get() = _storeList

    private val _storeName = MutableLiveData<String>()
    val storeName : LiveData<String>
        get() = _storeName

    init {
        updateStoreName("Steam")
    }

    fun refreshDeals(request: DealsRequest): Flow<PagingData<Deals>>? {
        val lastResult = _currentDealsResult
        val lastRequest = currentDealsRequest
        val storeId = request.storeID
        val lowerPrice = request.lowerPrice
        val upperPrice = request.upperPrice
        val title = request.title
        val aaa = request.AAA

        Timber.i("$storeId || ${lastRequest?.storeID} || $lowerPrice || ${lastRequest?.lowerPrice} " +
                "|| $upperPrice || ${lastRequest?.upperPrice} || $title || ${lastRequest?.title} " +
                "|| $aaa || ${lastRequest?.AAA}")

        Timber.i("${storeId == lastRequest?.storeID} || ${lowerPrice == lastRequest?.lowerPrice}" +
                "|| ${upperPrice == lastRequest?.upperPrice} || ${title == lastRequest?.title} || " +
                "${aaa == lastRequest?.AAA}")

//        if (storeId == lastRequest?.storeID && lowerPrice == lastRequest?.lowerPrice
//            && upperPrice == lastRequest?.upperPrice && title == lastRequest?.title
//            && aaa == lastRequest?.AAA && _currentDealsResult != null) {
//                Timber.i("Return Last Result")
//            return lastResult
//        }

        if (request == currentDealsRequest && _currentDealsResult != null) {
            return lastResult
        }

        currentDealsRequest = request

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

    fun updateStoreName(storeName : String){
        _storeName.value = storeName
    }
}