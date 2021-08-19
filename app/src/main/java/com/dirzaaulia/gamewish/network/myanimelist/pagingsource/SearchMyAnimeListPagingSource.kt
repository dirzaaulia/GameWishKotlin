package com.dirzaaulia.gamewish.network.myanimelist.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dirzaaulia.gamewish.data.models.myanimelist.ParentNode
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.util.MYANIMELIST_STARTING_OFFSET
import timber.log.Timber

class SearchMyAnimeListPagingSource(
    private val service : MyAnimeListApiUrlService,
    private val code : Int,
    private val authorization : String,
    private val query : String?,
    private val year : String?,
    private val season : String?,
    private val myAnimeListSortStatus : String?
) : PagingSource<Int, ParentNode>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ParentNode> {
        val offset = params.key ?: MYANIMELIST_STARTING_OFFSET

        return try {
            val response = when (code) {
                1 -> query?.let { service.searchMyAnimeListAnime(authorization, it, offset) }
                2 -> query?.let { service.searchMyAnimeListManga(authorization, it, offset) }
                3 -> service.getMyAnimeListSeasonalAnime(authorization, year!!, season!!, offset)
                4 -> service.getMyAnimeListAnimeList(
                    authorization, "@me", "list_status", myAnimeListSortStatus, offset)
                5 -> service.getMyAnimeListMangaList(
                    authorization, "@me", "list_status", myAnimeListSortStatus, offset)
                else -> null
            }

            val result = response?.data

            if (result == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                if (response.paging?.next != null) {
                    Timber.i(response.paging.next)
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