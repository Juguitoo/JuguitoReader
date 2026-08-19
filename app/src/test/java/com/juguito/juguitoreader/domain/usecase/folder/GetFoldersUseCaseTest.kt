package com.juguito.juguitoreader.domain.usecase.folder

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.FolderRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetFoldersUseCaseTest {

    private lateinit var useCase: GetFoldersUseCase
    private val repository = mockk<FolderRepository>()

    @Before
    fun setup() {
        useCase = GetFoldersUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getAllFolders() } returns flowOf(emptyList())
        
        val result = useCase().first()
        
        assertThat(result).isEmpty()
    }
}
