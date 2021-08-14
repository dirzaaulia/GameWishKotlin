package com.dirzaaulia.gamewish.di

import com.dirzaaulia.gamewish.network.cheapshark.CheapSharkService
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListApiUrlService
import com.dirzaaulia.gamewish.network.myanimelist.MyAnimeListBaseUrlService
import com.dirzaaulia.gamewish.network.rawg.RawgService
import com.dirzaaulia.gamewish.repository.FirebaseRepository
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

    @Singleton
    @Provides
    fun provideMyAnimeListBaseUrlService() : MyAnimeListBaseUrlService {
        return MyAnimeListBaseUrlService.create()
    }

    @Singleton
    @Provides
    fun provideMyAnimeListApiUrlService() : MyAnimeListApiUrlService {
        return MyAnimeListApiUrlService.create()
    }

    @Provides
    fun provideFirebaseRepository() : FirebaseRepository {
        return FirebaseRepository()
    }
}