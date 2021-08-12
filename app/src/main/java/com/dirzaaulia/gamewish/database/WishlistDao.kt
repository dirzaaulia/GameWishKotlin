package com.dirzaaulia.gamewish.database

import androidx.room.*
import com.dirzaaulia.gamewish.data.models.rawg.Wishlist
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlist: Wishlist)

    @Delete
    suspend fun delete(wishlist: Wishlist)

    @Query("SELECT * FROM wishlist_table")
    fun getAllWishlist() : Flow<List<Wishlist>>

    @Query("SELECT * FROM wishlist_table WHERE name LIKE '%' || :gameName || '%'")
    fun getFilteredWishlist(gameName : String) : Flow<List<Wishlist>>

    @Query("SELECT * FROM wishlist_table WHERE id = :gameId LIMIT 1")
    fun getWishlist(gameId: Int) : Flow<Wishlist>
}