package com.dirzaaulia.gamewish.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.util.DATABASE_NAME

@Database(entities = [Wishlist::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wishlistDao(): WishlistDao

    companion object {

        @Volatile private var instance : AppDatabase? = null

        fun getInstance(context : Context) : AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}