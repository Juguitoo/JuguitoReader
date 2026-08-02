package com.juguito.juguitoreader.di

import android.app.Application
import androidx.room.Room
import com.juguito.juguitoreader.data.local.JuguitoDatabase
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
    fun provideJuguitoDatabase(app: Application): JuguitoDatabase{
        return Room.databaseBuilder(
            app,
            JuguitoDatabase::class.java,
            "juguito_db"
        )
            .fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: JuguitoDatabase): BookDAO{
        return database.bookDAO
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: JuguitoDatabase): FolderDAO{
        return database.folderDAO
    }

    @Provides
    @Singleton
    fun provideGenreDao(database: JuguitoDatabase): GenreDAO{
        return database.genreDAO
    }
}