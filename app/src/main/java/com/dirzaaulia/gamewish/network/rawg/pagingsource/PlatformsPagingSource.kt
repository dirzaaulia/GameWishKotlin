package com.dirzaaulia.gamewish.network.rawg.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dirzaaulia.gamewish.data.models.rawg.Platform
import com.dirzaaulia.gamewish.network.rawg.RawgService
import com.dirzaaulia.gamewish.util.RAWG_KEY
import com.dirzaaulia.gamewish.util.RAWG_STARTING_PAGE_INDEX

class PlatformsPagingSource(
    private val service: RawgService
) : PagingSource<Int, Platform>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Platform> {
        val page = params.key ?: RAWG_STARTING_PAGE_INDEX

        return try {
            val response = service.getPlatforms(RAWG_KEY, page, 10)

            val platforms = response.results

            if (platforms == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                if (response.next != null) {
                    LoadResult.Page(
                        data = platforms,
                        prevKey = if (page == RAWG_STARTING_PAGE_INDEX) null else page - 1,
                        nextKey = page + 1
                    )
                } else {
                    LoadResult.Page(
                        data = platforms,
                        prevKey = null,
                        nextKey = null
                    )
                }
            }

        } catch (exception : Exception) {
            LoadResult.Error(exception)
        }
    }

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, Platform>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}