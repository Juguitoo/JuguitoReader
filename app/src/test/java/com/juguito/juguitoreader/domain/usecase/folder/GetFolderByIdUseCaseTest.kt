package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetFolderByIdUseCaseTest {

    private lateinit var useCase: GetFolderByIdUseCase
    private val repository = mockk<FolderRepository>()

    @Before
    fun setup() {
        useCase = GetFolderByIdUseCase(repository)
    }

    @Test
    fun `invoke returns folder from repository`() = runTest {
        val folder = Folder(id = 1, name = "F", colorHex = "#000")
        coEvery { repository.getFolderById(1) } returns folder
        val result = useCase(1)
        assertThat(result).isEqualTo(folder)
    }
}
