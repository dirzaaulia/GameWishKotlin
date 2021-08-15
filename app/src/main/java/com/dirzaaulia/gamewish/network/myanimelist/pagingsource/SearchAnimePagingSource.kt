package com.dirzaaulia.gamewish.network.myanimelist.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dirzaaulia.gamewish.data.models.myanimelist.Node
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.util.MYANIMELIST_STARTING_OFFSET

class SearchAnimePagingSource(
    private val service: MyAnimeListApiUrlService,
    private val authorization : String,
    private val query: String,
) : PagingSource<Int, ParentNode>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ParentNode> {
        val offset = params.key ?: MYANIMELIST_STARTING_OFFSET

        return try {
            val response = service.searchMyAnimeListAnime(authorization, query)
            val result = response.data

            if (result == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                if (response.paging?.next != null) {
                    LoadResult.Page(
                        data = result,
                        prevKey = if (offset == MYANIMELIST_STARTING_OFFSET) null else offset - 10,
                        nextKey = offset + 10
                    )
                } else {
                    LoadResult.Page(
                        data = result,
                        prevKey = null,
                        nextKey = null
                    )
                }
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, ParentNode>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}