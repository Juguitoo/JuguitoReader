package com.juguito.juguitoreader.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookGenreCrossRef
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.domain.enums.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreDAOTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var genreDAO: GenreDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        genreDAO = database.genreDAO
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertGenre_and_getByName() = runBlocking {
        val genre = GenreEntity(name = "Fantasy", createdAt = 0L)
        genreDAO.insertGenre(genre)
        
        val result = genreDAO.getGenreByName("Fantasy")
        assertThat(result?.name).isEqualTo("Fantasy")
    }

    @Test
    fun getGenresWithBookCount_returns_correct_count() = runBlocking {
        val genre = GenreEntity(id = 1, name = "Action", createdAt = 0L)
        val book1 = BookEntity(id = 1, title = "B1", author = "A", isPhysical = false, createdAt = 0L)
        val book2 = BookEntity(id = 2, title = "B2", author = "A", isPhysical = false, createdAt = 1L)
        
        genreDAO.insertGenre(genre)
        database.bookDAO.insertBooks(listOf(book1, book2))
        database.bookDAO.insertBookGenreCrossRefs(listOf(BookGenreCrossRef(1, 1), BookGenreCrossRef(2, 1)))
        
        val result = genreDAO.getGenresWithBookCount().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].genre.name).isEqualTo("Action")
        assertThat(result[0].bookCount).isEqualTo(2)
    }

    @Test
    fun getUnsyncedGenres_returns_only_unsynced() = runBlocking {
        val g1 = GenreEntity(name = "S", syncStatus = SyncStatus.SYNCED, createdAt = 0L)
        val g2 = GenreEntity(name = "U", syncStatus = SyncStatus.PENDING_UPDATE, createdAt = 1L)
        
        genreDAO.insertGenres(listOf(g1, g2))
        
        val unsynced = genreDAO.getUnsyncedGenres()
        assertThat(unsynced).hasSize(1)
        assertThat(unsynced[0].name).isEqualTo("U")
    }

    @Test
    fun deleteGenreById_removes_it() = runBlocking {
        genreDAO.insertGenre(GenreEntity(id = 1, name = "G", createdAt = 0L))
        genreDAO.deleteGenreById(1)
        assertThat(genreDAO.getGenreByName("G")).isNull()
    }

    @Test
    fun updateGenre_preservesBookGenreCrossRefs() = runBlocking {
        val genre = GenreEntity(id = 1, name = "Original", createdAt = 0L)
        val book = BookEntity(id = 1, title = "B", author = "A", isPhysical = false, createdAt = 0L)

        genreDAO.insertGenre(genre)
        database.bookDAO.insertBook(book)
        database.bookDAO.insertBookGenreCrossRefs(listOf(BookGenreCrossRef(bookId = 1, genreId = 1)))

        genreDAO.updateGenre(genre.copy(name = "Renamed"))

        assertThat(genreDAO.getGenreByName("Renamed")?.id).isEqualTo(1)

        val bookWithDetails = database.bookDAO.getBookById(1)
        assertThat(bookWithDetails).isNotNull()
        assertThat(bookWithDetails?.genres).hasSize(1)
        assertThat(bookWithDetails?.genres?.first()?.id).isEqualTo(1)
        assertThat(bookWithDetails?.genres?.first()?.name).isEqualTo("Renamed")
    }
}
