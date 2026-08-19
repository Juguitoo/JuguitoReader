package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddFolderUseCaseTest {

    private lateinit var useCase: AddFolderUseCase
    private val repository = mockk<FolderRepository>()

    @Before
    fun setup() {
        useCase = AddFolderUseCase(repository)
    }

    @Test
    fun `invoke with empty name returns failure`() = runBlocking {
        val folder = Folder(name = "", colorHex = "#000")
        val result = useCase(folder)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("nombre")
    }

    @Test
    fun `invoke with existing name returns failure`() = runBlocking {
        val folder = Folder(name = "Existing", colorHex = "#000")
        coEvery { repository.getFolderByName("Existing") } returns folder
        
        val result = useCase(folder)
        
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Ya existe")
    }

    @Test
    fun `invoke with valid folder calls save and returns success`() = runBlocking {
        val folder = Folder(name = "New", colorHex = "#000")
        coEvery { repository.getFolderByName("New") } returns null
        coEvery { repository.saveFolder(any()) } returns 1L
        
        val result = useCase(folder)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.saveFolder(any()) }
    }
}
