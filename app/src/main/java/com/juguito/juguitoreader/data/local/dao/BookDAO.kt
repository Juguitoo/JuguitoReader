package com.juguito.juguitoreader.data.local.dao

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.BookWithDetails

@Dao
interface BookDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookFolderCrossRefs(crossRefs: List<BookFolderCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookGenreCrossRefs(crossRefs: List<BookGenreCrossRef>)

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BookWithDetails?

    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookWithDetails>>

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Int)

    @Query("SELECT * FROM books WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedBooks(): List<BookEntity>
}