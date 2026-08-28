package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>

    suspend fun getBookById(id: Int): Book?

    suspend fun insertBook(book: Book): Long

    suspend fun insertBooks(books: List<Book>)

    suspend fun updateBook(book: Book)

    suspend fun updateBooks(books: List<Book>)

    suspend fun syncCrossReferences(bookId: Int, folderIds: List<Int>, genreIds: List<Int>)

    suspend fun deleteBook(id: Int)

    suspend fun syncPendingBooks()
}