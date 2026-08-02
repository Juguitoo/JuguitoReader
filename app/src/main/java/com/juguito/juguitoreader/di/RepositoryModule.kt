package com.juguito.juguitoreader.di

import com.juguito.juguitoreader.data.repository.BookRepositoryImpl
import com.juguito.juguitoreader.data.repository.FolderRepositoryImpl
import com.juguito.juguitoreader.data.repository.GenreRepositoryImpl
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
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
}