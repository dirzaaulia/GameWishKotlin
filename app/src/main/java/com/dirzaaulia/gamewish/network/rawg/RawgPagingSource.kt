package com.dirzaaulia.gamewish.network.rawg

import androidx.paging.PagingSource
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.data.response.SearchGamesResponse
import com.dirzaaulia.gamewish.util.RAWG_KEY
import java.lang.Exception


private const val RAWG_STARTING_PAGE_INDEX = 1

class RawgPagingSource(
    private val service: RawgService,
    private val search: String
) : PagingSource<Int, Games>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Games> {
        val page = params.key ?: RAWG_STARTING_PAGE_INDEX

        return try {
            val response = service.searchGames(
                RAWG_KEY,
                page,
                10,
                true,
                search
            )

            val games = response.results

            LoadResult.Page(
                data = games!!,
                prevKey = if (page == RAWG_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = page + 1
            )

        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}