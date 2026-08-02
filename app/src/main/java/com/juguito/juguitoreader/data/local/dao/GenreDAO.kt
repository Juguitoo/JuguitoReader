package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("SELECT * FROM genres")
    suspend fun getAllGenres(): Flow<List<GenreEntity>>

    @Query("DELETE FROM genres WHERE id = :genreId")
    suspend fun deleteGenreById(genreId: Int)

    @Query("SELECT * FROM genres WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedGenres(): List<GenreEntity>
}