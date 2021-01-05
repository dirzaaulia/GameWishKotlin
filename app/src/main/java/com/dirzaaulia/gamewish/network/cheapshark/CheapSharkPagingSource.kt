package com.dirzaaulia.gamewish.network.cheapshark

import androidx.paging.PagingSource
import com.dirzaaulia.gamewish.data.models.Deals
import com.dirzaaulia.gamewish.data.models.DealsRequest
import java.lang.Exception

private const val CHEAPSHARK_STARTING_PAGE_INDEX = 0

class CheapSharkPagingSource (
    private val service: CheapSharkService,
    private val request: DealsRequest
) : PagingSource<Int, Deals>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Deals> {
        val page = params.key ?: CHEAPSHARK_STARTING_PAGE_INDEX

        return try {
            val response = service.getGameDeals(
                request.storeID,
                page,
                10,
                request.lowerPrice,
                request.upperPrice,
                request.title,
                request.AAA
            )

            if (response.isEmpty()) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                LoadResult.Page(
                    data = response,
                    prevKey = if (page == CHEAPSHARK_STARTING_PAGE_INDEX) null else page - 1,
                    nextKey = page + 1
                )
            }

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}