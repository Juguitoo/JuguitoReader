package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDAO: BookDAO
): BookRepository{
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDAO.getAllBooks().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getBookById(id: Int): Book? {
        return bookDAO.getBookById(id)?.toDomain()
    }

    override suspend fun insertBook(book: Book): Long {
        return bookDAO.insertBook(book.toEntity())
    }

    override suspend fun insertBooks(books: List<Book>) {
        bookDAO.insertBooks(books.map { it.toEntity() })
    }

    override suspend fun updateBook(book: Book) {
        bookDAO.updateBook(book.toEntity())
    }

    override suspend fun updateBooks(books: List<Book>) {
        bookDAO.updateBooks(books.map { it.toEntity() })
    }

    override suspend fun syncCrossReferences(bookId: Int, folderIds: List<Int>, genreIds: List<Int>) {
        bookDAO.syncBookCrossRefs(bookId, folderIds, genreIds)
    }

    override suspend fun deleteBook(id: Int) {
        bookDAO.deleteBookById(id)
    }

    override suspend fun syncPendingBooks() {
        TODO("Not yet implemented")
    }
}