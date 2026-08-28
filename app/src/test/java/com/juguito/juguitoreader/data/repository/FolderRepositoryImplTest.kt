package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.dao.FolderDAO
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.FolderWithCountEntity
import com.juguito.juguitoreader.domain.model.Folder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FolderRepositoryImplTest {

    private lateinit var repository: FolderRepositoryImpl
    private val folderDAO = mockk<FolderDAO>()

    @Before
    fun setup() {
        repository = FolderRepositoryImpl(folderDAO)
    }

    @Test
    fun `getAllFolders maps entities to domain`() = runTest {
        val folderEntity = FolderEntity(id = 1, name = "Reading", colorHex = "#000", createdAt = 0L)
        val folderWithCount = FolderWithCountEntity(folder = folderEntity, bookCount = 2)

        every { folderDAO.getFoldersWithBookCount() } returns flowOf(listOf(folderWithCount))

        val result = repository.getAllFolders().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Reading")
    }

    @Test
    fun `getFolderById returns domain folder`() = runTest {
        val folderEntity = FolderEntity(id = 1, name = "F1", colorHex = "#000", createdAt = 0L)
        coEvery { folderDAO.getFolderById(1) } returns folderEntity

        val result = repository.getFolderById(1)

        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("F1")
    }

    @Test
    fun `getFolderByName returns domain folder`() = runTest {
        val folderEntity = FolderEntity(id = 1, name = "F1", colorHex = "#000", createdAt = 0L)
        coEvery { folderDAO.getFolderByName("F1") } returns folderEntity

        val result = repository.getFolderByName("F1")

        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("F1")
    }

    @Test
    fun `insertFolder calls insertFolder on DAO`() = runTest {
        val folder = Folder(name = "To Read", colorHex = "#FFF")
        coEvery { folderDAO.insertFolder(any()) } returns 1L

        val id = repository.insertFolder(folder)

        assertThat(id).isEqualTo(1L)
        coVerify { folderDAO.insertFolder(any()) }
    }

    @Test
    fun `insertFolders calls insertFolders on DAO`() = runTest {
        val folders = listOf(Folder(name = "F1", colorHex = "#000"))
        coEvery { folderDAO.insertFolders(any()) } returns Unit

        repository.insertFolders(folders)

        coVerify { folderDAO.insertFolders(any()) }
    }

    @Test
    fun `updateFolder calls updateFolder on DAO`() = runTest {
        val folder = Folder(id = 1, name = "Renamed", colorHex = "#000")
        coEvery { folderDAO.updateFolder(any()) } returns Unit

        repository.updateFolder(folder)

        coVerify { folderDAO.updateFolder(any()) }
    }

    @Test
    fun `deleteFolder calls deleteFolderById on DAO`() = runTest {
        coEvery { folderDAO.deleteFolderById(1) } returns Unit

        repository.deleteFolder(1)

        coVerify { folderDAO.deleteFolderById(1) }
    }
}
