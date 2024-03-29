package com.dirzaaulia.gamewish.di

import android.content.Context
import com.dirzaaulia.gamewish.database.AppDatabase
import com.dirzaaulia.gamewish.database.WishlistDao
import com.dirzaaulia.gamewish.repository.ProtoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideWishlistDao(appDatabase: AppDatabase): WishlistDao {
        return appDatabase.wishlistDao()
    }

    @Singleton
    @Provides
    fun provideProtoRepository(@ApplicationContext context: Context): ProtoRepository {
        return ProtoRepository(context)
    }
}