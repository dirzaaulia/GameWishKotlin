package com.dirzaaulia.gamewish.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.models.rawg.Stores
import com.dirzaaulia.gamewish.data.request.DealsRequest
import com.dirzaaulia.gamewish.network.cheapshark.CheapSharkPagingSource
import com.dirzaaulia.gamewish.network.cheapshark.CheapSharkService
import com.dirzaaulia.gamewish.util.DEALS_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheapSharkRepository @Inject constructor (private val service: CheapSharkService)  {

    fun refreshDeals(request: DealsRequest) : Flow<PagingData<Deals>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = DEALS_PAGE_SIZE),
            pagingSourceFactory = { CheapSharkPagingSource(service, request) }
        ).flow
    }

    suspend fun getAllStores() : List<Stores> {
        return withContext(Dispatchers.IO) {
               service.getStoresList()
        }
    }
}