package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.GenreWithCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: GenreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("SELECT * FROM genres WHERE name = :genreName")
    suspend fun getGenreByName(genreName: String): GenreEntity?

    @Query("""
        SELECT 
            g.*, 
            (SELECT COUNT(*) FROM book_genres bg WHERE bg.genre_id = g.id) AS bookCount
        FROM genres g
        ORDER BY g.name ASC
    """)
    fun getGenresWithBookCount(): Flow<List<GenreWithCountEntity>>

    @Query("SELECT name FROM genres")
    fun getAllGenreNames(): List<String>

    @Query("DELETE FROM genres WHERE id = :genreId")
    suspend fun deleteGenreById(genreId: Int)

    @Query("SELECT * FROM genres WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedGenres(): List<GenreEntity>
}