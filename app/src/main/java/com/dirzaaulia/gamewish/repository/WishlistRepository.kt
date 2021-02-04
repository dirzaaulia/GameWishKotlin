package com.dirzaaulia.gamewish.repository

import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.database.WishlistDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao
) {
    fun getWishlist(gameId: Int) = wishlistDao.getWishlist(gameId)

    fun getAllWishlist() = wishlistDao.getAllWishlist()

    fun isWishlisted(gamesId: Int) = wishlistDao.isWishlisted(gamesId)

    suspend fun removeFromWishlist(wishlist: Wishlist) {
        return wishlistDao.delete(wishlist)
    }

    suspend fun addToWishlist(wishlist: Wishlist) {
        wishlistDao.insert(wishlist)
    }
}