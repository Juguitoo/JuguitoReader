package com.juguito.juguitoreader.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookFolderCrossRef
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderDAOTest {

    private lateinit var database: JuguitoReaderDatabase
    private lateinit var folderDAO: FolderDAO

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JuguitoReaderDatabase::class.java
        ).allowMainThreadQueries().build()
        folderDAO = database.folderDAO
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertFolders_and_getById() = runBlocking {
        val folders = listOf(
            FolderEntity(id = 1, name = "F1", colorHex = "#000", createdAt = 0L),
            FolderEntity(id = 2, name = "F2", colorHex = "#FFF", createdAt = 1L)
        )
        folderDAO.insertFolders(folders)
        
        assertThat(folderDAO.getFolderById(1)?.name).isEqualTo("F1")
        assertThat(folderDAO.getFolderById(2)?.name).isEqualTo("F2")
    }

    @Test
    fun getFoldersWithBookCount_returns_correct_count() = runBlocking {
        val folder = FolderEntity(id = 1, name = "My Folder", colorHex = "#000", createdAt = 0L)
        val book = BookEntity(id = 1, title = "B", author = "A", isPhysical = false, createdAt = 0L)
        
        folderDAO.insertFolder(folder)
        database.bookDAO.insertBook(book)
        database.bookDAO.insertBookFolderCrossRefs(listOf(BookFolderCrossRef(1, 1)))
        
        val result = folderDAO.getFoldersWithBookCount().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookCount).isEqualTo(1)
    }

    @Test
    fun deleteFolderById_removes_it() = runBlocking {
        folderDAO.insertFolder(FolderEntity(id = 1, name = "F", colorHex = "#000", createdAt = 0L))
        folderDAO.deleteFolderById(1)
        assertThat(folderDAO.getFolderById(1)).isNull()
    }

    @Test
    fun updateFolder_preservesBookFolderCrossRefs() = runBlocking {
        val folder = FolderEntity(id = 1, name = "Original", colorHex = "#000", createdAt = 0L)
        val book = BookEntity(id = 1, title = "B", author = "A", isPhysical = false, createdAt = 0L)

        folderDAO.insertFolder(folder)
        database.bookDAO.insertBook(book)
        database.bookDAO.insertBookFolderCrossRefs(listOf(BookFolderCrossRef(bookId = 1, folderId = 1)))

        folderDAO.updateFolder(folder.copy(name = "Renamed"))

        assertThat(folderDAO.getFolderById(1)?.name).isEqualTo("Renamed")

        val bookWithDetails = database.bookDAO.getBookById(1)
        assertThat(bookWithDetails).isNotNull()
        assertThat(bookWithDetails?.folders).hasSize(1)
        assertThat(bookWithDetails?.folders?.first()?.id).isEqualTo(1)
        assertThat(bookWithDetails?.folders?.first()?.name).isEqualTo("Renamed")
    }
}
