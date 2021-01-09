package com.dirzaaulia.gamewish.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dirzaaulia.gamewish.data.models.Wishlist
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlist: Wishlist)

    @Query("SELECT EXISTS(SELECT * FROM wishlist_table WHERE id = :gameId LIMIT 1)")
    fun isAdded(gameId: Int) : Flow<Boolean>

    @Query("SELECT * FROM wishlist_table WHERE id = :gameId LIMIT 1")
    suspend fun getWishlist(gameId: Int) : Wishlist
}