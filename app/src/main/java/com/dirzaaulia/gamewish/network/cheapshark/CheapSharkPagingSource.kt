package com.dirzaaulia.gamewish.network.cheapshark

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dirzaaulia.gamewish.data.models.cheapshark.Deals
import com.dirzaaulia.gamewish.data.request.DealsRequest
import timber.log.Timber

private const val CHEAPSHARK_STARTING_PAGE_INDEX = 0

class CheapSharkPagingSource (
    private val service: CheapSharkService,
    private val request: DealsRequest
) : PagingSource<Int, Deals>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Deals> {
        val page = params.key ?: CHEAPSHARK_STARTING_PAGE_INDEX
        Timber.i("${request.storeID}, $page, 10,${request.lowerPrice}, ${request.upperPrice}, ${request.title}, " +
                "${request.AAA}")
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

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, Deals>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}