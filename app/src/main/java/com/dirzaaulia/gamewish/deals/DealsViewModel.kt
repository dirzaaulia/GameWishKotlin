package com.dirzaaulia.gamewish.deals

import android.app.Application
import androidx.lifecycle.*
import com.dirzaaulia.gamewish.models.Deals
import com.dirzaaulia.gamewish.network.Network
import com.dirzaaulia.gamewish.repository.Repository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class DealsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository()

    private val _deals = MutableLiveData<List<Deals>>()
    val deals: LiveData<List<Deals>>
        get() = _deals

    init {
       refreshDeals()
    }

    private fun refreshDeals() {
        viewModelScope.launch {
            try {
                val listResult = repository.refreshDeals()

                if (listResult.isNotEmpty()) {
                    _deals.value = listResult
                } else {
                    Timber.i("Deals Empty!")
                }
            } catch (e: Exception) {
                Timber.i(e.localizedMessage)
            }
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DealsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DealsViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }

    }
}