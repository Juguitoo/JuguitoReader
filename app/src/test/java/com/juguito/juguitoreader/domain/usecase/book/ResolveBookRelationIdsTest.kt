package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResolveBookRelationIdsTest {

    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        coEvery { folderRepository.getFolderById(any()) } returns null
        coEvery { folderRepository.getFolderByName(any()) } returns null
        coEvery { genreRepository.getGenreById(any()) } returns null
        coEvery { genreRepository.getGenreByName(any()) } returns null
    }

    @Test
    fun `resolveFolderIds uses existing id and does not look up by name`() = runTest {
        val folder = Folder(id = 5, name = "Sci-Fi-OLD", colorHex = "#FFF")
        coEvery { folderRepository.getFolderById(5) } returns Folder(id = 5, name = "Science Fiction", colorHex = "#FFF")

        val ids = resolveFolderIds(listOf(folder), folderRepository)

        assertThat(ids).containsExactly(5)
        coVerify(exactly = 0) { folderRepository.getFolderByName(any()) }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
    }

    @Test
    fun `resolveFolderIds omits stale id instead of inserting by name`() = runTest {
        val folder = Folder(id = 5, name = "Sci-Fi", colorHex = "#FFF")

        val ids = resolveFolderIds(listOf(folder), folderRepository)

        assertThat(ids).isEmpty()
        coVerify(exactly = 0) { folderRepository.getFolderByName(any()) }
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
    }

    @Test
    fun `resolveFolderIds creates folder when id is zero and name is new`() = runTest {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        coEvery { folderRepository.insertFolder(folder) } returns 9L

        val ids = resolveFolderIds(listOf(folder), folderRepository)

        assertThat(ids).containsExactly(9)
        coVerify(exactly = 1) { folderRepository.getFolderByName("Sci-Fi") }
        coVerify(exactly = 1) { folderRepository.insertFolder(folder) }
    }

    @Test
    fun `resolveFolderIds reuses folder found by name when id is zero`() = runTest {
        val folder = Folder(id = 0, name = "Sci-Fi", colorHex = "#FFF")
        coEvery { folderRepository.getFolderByName("Sci-Fi") } returns Folder(id = 7, name = "Sci-Fi", colorHex = "#FFF")

        val ids = resolveFolderIds(listOf(folder), folderRepository)

        assertThat(ids).containsExactly(7)
        coVerify(exactly = 0) { folderRepository.insertFolder(any()) }
    }

    @Test
    fun `resolveFolderIds keeps valid ids and omits stale ones in the same list`() = runTest {
        val kept = Folder(id = 2, name = "Kept", colorHex = "#111")
        val stale = Folder(id = 8, name = "Deleted", colorHex = "#222")
        val created = Folder(id = 0, name = "New", colorHex = "#333")
        coEvery { folderRepository.getFolderById(2) } returns kept
        coEvery { folderRepository.insertFolder(created) } returns 11L

        val ids = resolveFolderIds(listOf(kept, stale, created), folderRepository)

        assertThat(ids).containsExactly(2, 11).inOrder()
        coVerify(exactly = 0) { folderRepository.getFolderByName("Deleted") }
        coVerify(exactly = 0) { folderRepository.insertFolder(stale) }
    }

    @Test
    fun `resolveGenreIds uses existing id and does not look up by name`() = runTest {
        val genre = Genre(id = 3, name = "Fairy")
        coEvery { genreRepository.getGenreById(3) } returns Genre(id = 3, name = "Fantasy")

        val ids = resolveGenreIds(listOf(genre), genreRepository)

        assertThat(ids).containsExactly(3)
        coVerify(exactly = 0) { genreRepository.getGenreByName(any()) }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `resolveGenreIds omits stale id instead of inserting by name`() = runTest {
        val genre = Genre(id = 3, name = "Fantasy")

        val ids = resolveGenreIds(listOf(genre), genreRepository)

        assertThat(ids).isEmpty()
        coVerify(exactly = 0) { genreRepository.getGenreByName(any()) }
        coVerify(exactly = 0) { genreRepository.insertGenre(any()) }
    }

    @Test
    fun `resolveGenreIds creates genre when id is zero and name is new`() = runTest {
        val genre = Genre(id = 0, name = "Fantasy")
        coEvery { genreRepository.insertGenre(genre) } returns 20L

        val ids = resolveGenreIds(listOf(genre), genreRepository)

        assertThat(ids).containsExactly(20)
        coVerify(exactly = 1) { genreRepository.getGenreByName("Fantasy") }
        coVerify(exactly = 1) { genreRepository.insertGenre(genre) }
    }
}
