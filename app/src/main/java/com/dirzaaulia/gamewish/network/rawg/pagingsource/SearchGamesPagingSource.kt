package com.dirzaaulia.gamewish.network.rawg.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dirzaaulia.gamewish.data.models.rawg.Games
import com.dirzaaulia.gamewish.network.rawg.RawgService
import com.dirzaaulia.gamewish.util.RAWG_KEY
import com.dirzaaulia.gamewish.util.RAWG_STARTING_PAGE_INDEX

class SearchGamesPagingSource(
    private val service: RawgService,
    private val search: String?,
    private val genres: Int?,
    private val publishers: Int?,
    private val platforms: Int?
) : PagingSource<Int, Games>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Games> {
        val page = params.key ?: RAWG_STARTING_PAGE_INDEX

        return try {
            val response = service.searchGames(
                RAWG_KEY,
                page,
                10,
                true,
                search,
                genres,
                publishers,
                platforms
            )

            val games = response.results

            if (games == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                if (response.next != null) {
                    LoadResult.Page(
                        data = games,
                        prevKey = if (page == RAWG_STARTING_PAGE_INDEX) null else page - 1,
                        nextKey = page + 1
                    )
                } else {
                    LoadResult.Page(
                        data = games,
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
    override fun getRefreshKey(state: PagingState<Int, Games>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}