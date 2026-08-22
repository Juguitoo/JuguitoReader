package com.juguito.juguitoreader.di

import com.juguito.juguitoreader.data.repository.BookRepositoryImpl
import com.juguito.juguitoreader.data.repository.DailyReadingRepositoryImpl
import com.juguito.juguitoreader.data.repository.FolderRepositoryImpl
import com.juguito.juguitoreader.data.repository.GenreRepositoryImpl
import com.juguito.juguitoreader.data.repository.ReadingProgressRepositoryImpl
import com.juguito.juguitoreader.data.repository.SettingsRepositoryImpl
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        folderRepositoryImpl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindGenreRepository(
        genreRepositoryImpl: GenreRepositoryImpl
    ): GenreRepository

    @Binds
    @Singleton
    abstract fun bindReadingProgressRepository(
        readingProgressRepositoryImpl: ReadingProgressRepositoryImpl
    ): ReadingProgressRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDailyReadingRepository(
        dailyReadingRepositoryImpl: DailyReadingRepositoryImpl
    ): DailyReadingRepository
}