package com.juguito.juguitoreader.data.repository

import androidx.room.withTransaction
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.dao.DailyReadingDAO
import com.juguito.juguitoreader.data.local.dao.ReadingProgressDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.model.DeletedBookSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDAO: BookDAO,
    private val readingProgressDAO: ReadingProgressDAO,
    private val dailyReadingDAO: DailyReadingDAO,
    private val database: JuguitoReaderDatabase
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

    override suspend fun updateBookWithNewEpub(book: Book, folderIds: List<Int>, genreIds: List<Int>) = database.withTransaction {
        updateBookWithCrossRefs(book, folderIds, genreIds)
        readingProgressDAO.deleteReadingProgressById(book.id)
        dailyReadingDAO.deleteBookDailyReadings(book.id)
    }

    override suspend fun restoreDeletedBook(snapshot: DeletedBookSnapshot, folderIds : List<Int>, genreIds : List<Int>) = database.withTransaction {
        insertBookWithCrossRefs(snapshot.book, folderIds, genreIds)
        snapshot.dailyReadings.forEach { dailyReadingDAO.insert(it.toEntity()) }
        if (snapshot.readingProgress != null) {
            readingProgressDAO.insertReadingProgress(snapshot.readingProgress.toEntity())
        }
    }

    override suspend fun insertBookWithCrossRefs(book: Book, folderIds: List<Int>, genreIds: List<Int>) : Long = database.withTransaction {
        val id = bookDAO.insertBook(book.toEntity())
        bookDAO.syncBookCrossRefs(id.toInt(), folderIds, genreIds)
        return@withTransaction id
    }

    override suspend fun updateBookWithCrossRefs(book: Book, folderIds: List<Int>, genreIds: List<Int>) {
        bookDAO.updateBook(book.toEntity())
        bookDAO.syncBookCrossRefs(book.id, folderIds, genreIds)
    }

    override suspend fun syncPendingBooks() {
        TODO("Not yet implemented")
    }
}