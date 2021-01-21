package com.dirzaaulia.gamewish.database

import androidx.room.*
import com.dirzaaulia.gamewish.data.models.GameDetails
import com.dirzaaulia.gamewish.data.models.Wishlist
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlist: Wishlist)

    @Delete
    suspend fun delete(wishlist: Wishlist)

    @Query("SELECT EXISTS(SELECT * FROM wishlist_table WHERE id = :gameId LIMIT 1)")
    fun isAdded(gameId: Int) : Flow<Boolean>

    @Query("SELECT * FROM wishlist_table WHERE id = :gameId LIMIT 1")
    suspend fun getWishlist(gameId: Int) : Wishlist
}