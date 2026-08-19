package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteFolderUseCaseTest {

    private lateinit var useCase: DeleteFolderUseCase
    private val repository = mockk<FolderRepository>()

    @Before
    fun setup() {
        useCase = DeleteFolderUseCase(repository)
    }

    @Test
    fun `invoke calls delete on repository`() = runTest {
        coEvery { repository.deleteFolder(1) } returns Unit
        
        val result = useCase(1)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.deleteFolder(1) }
    }
}
