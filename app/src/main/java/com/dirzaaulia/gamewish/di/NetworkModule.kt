package com.dirzaaulia.gamewish.di

import com.dirzaaulia.gamewish.network.CheapSharkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideUnsplashService(): CheapSharkService {
        return CheapSharkService.create()
    }
}