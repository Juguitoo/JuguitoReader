package com.juguito.juguitoreader.di

import android.app.Application
import androidx.room.Room
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.dao.FolderDAO
import com.juguito.juguitoreader.data.local.dao.GenreDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJuguitoDatabase(app: Application): JuguitoReaderDatabase {
        return Room.databaseBuilder(
            app,
            JuguitoReaderDatabase::class.java,
            "juguito_db"
        )
            .fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: JuguitoReaderDatabase): BookDAO{
        return database.bookDAO
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: JuguitoReaderDatabase): FolderDAO{
        return database.folderDAO
    }

    @Provides
    @Singleton
    fun provideGenreDao(database: JuguitoReaderDatabase): GenreDAO{
        return database.genreDAO
    }
}