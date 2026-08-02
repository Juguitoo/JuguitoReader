package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun getAllBooks(): Flow<List<Book>>

    suspend fun getBookById(id: Int): Book?

    suspend fun saveBook(book: Book)

    suspend fun saveBooks(book: List<Book>)

    suspend fun deleteBook(id: Int)

    suspend fun syncPendingBooks()
}