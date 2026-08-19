package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateFolderUseCaseTest {

    private lateinit var useCase: UpdateFolderUseCase
    private val repository = mockk<FolderRepository>()

    @Before
    fun setup() {
        useCase = UpdateFolderUseCase(repository)
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val result = useCase(Folder(id = 1, name = "", colorHex = "#000"))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `invoke with valid data returns success`() = runTest {
        val folder = Folder(id = 1, name = "F1", colorHex = "#000")
        coEvery { repository.getFolderByName("F1") } returns folder
        coEvery { repository.saveFolder(any()) } returns 1L
        
        val result = useCase(folder)
        
        assertThat(result.isSuccess).isTrue()
    }
}
