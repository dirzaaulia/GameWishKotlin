package com.dirzaaulia.gamewish.repository

import androidx.lifecycle.LiveData
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.database.WishlistDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao
) {

    suspend fun addToWishlist(wishlist: Wishlist) {
        wishlistDao.insert(wishlist)
    }

    suspend fun getWistlist(gameId: Int) : Wishlist {
        return wishlistDao.getWishlist(gameId)
    }

    fun isAdded(gamesId: Int) = wishlistDao.isAdded(gamesId)
}