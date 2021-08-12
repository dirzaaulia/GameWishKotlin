package com.dirzaaulia.gamewish.repository

import com.dirzaaulia.gamewish.data.models.rawg.Wishlist
import com.dirzaaulia.gamewish.database.WishlistDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao
) {
    fun getWishlist(gameId: Int) = wishlistDao.getWishlist(gameId)

    fun getAllWishlist() = wishlistDao.getAllWishlist()

    fun getFilteredWishlist(gameName : String) : Flow<List<Wishlist>> {
        return wishlistDao.getFilteredWishlist(gameName)
            .flowOn(Dispatchers.Default)
            .conflate()
    }

    suspend fun removeFromWishlist(wishlist: Wishlist) {
        return wishlistDao.delete(wishlist)
    }

    suspend fun addToWishlist(wishlist: Wishlist) {
        wishlistDao.insert(wishlist)
    }
}