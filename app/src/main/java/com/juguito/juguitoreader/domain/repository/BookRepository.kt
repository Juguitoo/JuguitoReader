package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun getAllBooks(): Flow<List<Book>>

    suspend fun getBookById(id: Int): Book?

    suspend fun saveBook(book: Book): Long

    suspend fun saveBooks(books: List<Book>)

    suspend fun addCrossReferences(bookId: Int, folderIds: List<Int>, genreIds: List<Int>)

    suspend fun deleteBook(id: Int)

    suspend fun syncPendingBooks()
}