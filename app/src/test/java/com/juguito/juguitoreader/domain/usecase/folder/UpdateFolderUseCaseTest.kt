package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
    fun `invoke with duplicate name from another folder returns failure`() = runTest {
        val folder = Folder(id = 1, name = "Taken", colorHex = "#000")
        coEvery { repository.getFolderByName("Taken") } returns Folder(id = 2, name = "Taken", colorHex = "#FFF")

        val result = useCase(folder)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_folder_exists)
    }

    @Test
    fun `invoke with valid data calls updateFolder and returns success`() = runTest {
        val folder = Folder(id = 1, name = "F1", colorHex = "#000")
        coEvery { repository.getFolderByName("F1") } returns folder
        coEvery { repository.updateFolder(any()) } returns Unit

        val result = useCase(folder)

        assertThat(result.isSuccess).isTrue()
        coVerify { repository.updateFolder(folder) }
    }
}
