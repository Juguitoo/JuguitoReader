package com.juguito.juguitoreader.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookmarkEntity
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkDAOTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var bookmarkDAO: BookmarkDAO
    private lateinit var bookDAO: BookDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        bookmarkDAO = database.bookmarkDAO
        bookDAO = database.bookDAO
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun deleteBook_removes_its_bookmarks_and_keeps_the_other_books() = runBlocking {
        insertBook(1)
        insertBook(2)
        bookmarkDAO.insertBookmark(bookmark(bookId = 1, quote = "mapa"))
        bookmarkDAO.insertBookmark(bookmark(bookId = 2, quote = "frase"))

        bookDAO.deleteBookById(1)

        assertThat(bookmarkDAO.getBookBookmarks(1).first()).isEmpty()
        assertThat(bookmarkDAO.getBookBookmarks(2).first()).hasSize(1)
    }

    private fun insertBook(id: Int) = runBlocking {
        bookDAO.insertBook(
            BookEntity(id = id, title = "Title", author = "Author", isPhysical = false, createdAt = 0L)
        )
    }

    private fun bookmark(bookId: Int, quote: String) = BookmarkEntity(
        bookId = bookId,
        kind = BookmarkKind.POSITION,
        chapterHref = "chapter.xhtml",
        quote = quote,
        prefix = "",
        suffix = "",
        imageSrc = null,
        color = null,
        noteText = null,
        scrollPosition = 0.2f,
        createdAt = 1L
    )
}
