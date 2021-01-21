package com.dirzaaulia.gamewish.di

import com.dirzaaulia.gamewish.network.cheapshark.CheapSharkService
import com.dirzaaulia.gamewish.network.rawg.RawgService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideCheapSharkService(): CheapSharkService {
        return CheapSharkService.create()
    }

    @Singleton
    @Provides
    fun provideRawgServer(): RawgService {
        return RawgService.create()
    }
}