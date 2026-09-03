package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.BookWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDAO {

    @Insert
    suspend fun insertBook(book: BookEntity): Long

    @Insert
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Update
    suspend fun updateBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookFolderCrossRefs(crossRefs: List<BookFolderCrossRef>)

    @Query("DELETE FROM book_folders WHERE book_id = :bookId")
    suspend fun deleteBookFolderCrossRefs(bookId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookGenreCrossRefs(crossRefs: List<BookGenreCrossRef>)

    @Query("DELETE FROM book_genres WHERE book_id = :bookId")
    suspend fun deleteBookGenreCrossRefs(bookId: Int)

    @Transaction
    suspend fun syncBookCrossRefs(bookId: Int, folderIds: List<Int>, genreIds: List<Int>) {
        deleteBookFolderCrossRefs(bookId)
        deleteBookGenreCrossRefs(bookId)

        if (folderIds.isNotEmpty()) insertBookFolderCrossRefs(folderIds.map { BookFolderCrossRef(bookId = bookId, folderId = it) })
        if (genreIds.isNotEmpty()) insertBookGenreCrossRefs(genreIds.map { BookGenreCrossRef(bookId = bookId, genreId = it) })
    }

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BookWithDetails?

    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookWithDetails>>

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Int)
}