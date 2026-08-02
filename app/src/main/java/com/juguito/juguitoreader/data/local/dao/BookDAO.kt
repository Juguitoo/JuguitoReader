package com.juguito.juguitoreader.data.local.dao

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookWithDetails

@Dao
interface BookDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Query("SELECT * FROM books WHERE id = :bookId ")
    suspend fun getBookById(bookId: Int): BookEntity?

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedBooks(): List<BookEntity>

    @Transaction
    @Query("SELECT * FROM books ")
    suspend fun getAllBooksWithDetails(): Flow<List<BookWithDetails>>

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Int)
}