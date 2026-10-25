package com.juguito.juguitoreader.di

import com.juguito.juguitoreader.debug.AppInitializer
import com.juguito.juguitoreader.debug.PreviewStatsSeeder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class PreviewStatsModule {
    @Binds
    @IntoSet
    abstract fun bindPreviewStatsSeeder(seeder: PreviewStatsSeeder): AppInitializer
}
