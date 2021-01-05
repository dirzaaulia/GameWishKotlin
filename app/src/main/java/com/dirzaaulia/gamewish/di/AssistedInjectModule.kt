package com.dirzaaulia.gamewish.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@AssistedModule
@Module(includes = [AssistedInject_AssistedInjectModule::class])
@InstallIn(FragmentComponent::class)
interface AssistedInjectModule