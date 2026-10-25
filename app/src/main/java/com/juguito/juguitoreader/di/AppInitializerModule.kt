package com.juguito.juguitoreader.di

import com.juguito.juguitoreader.debug.AppInitializer
import dagger.Module
import dagger.multibindings.Multibinds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppInitializerModule {
    @Multibinds
    abstract fun initializers(): Set<AppInitializer>
}
