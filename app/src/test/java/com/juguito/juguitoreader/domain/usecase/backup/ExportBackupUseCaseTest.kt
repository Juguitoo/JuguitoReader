package com.juguito.juguitoreader.domain.usecase.backup

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExportBackupUseCaseTest {

    private lateinit var useCase: ExportBackupUseCase
    private val backupRepository = mockk<BackupRepository>()

    @Before
    fun setup() {
        useCase = ExportBackupUseCase(backupRepository)
    }

    @Test
    fun `invoke with blank destination returns failure without calling repository`() = runTest {
        val result = useCase("")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_backup_destination)
        coVerify(exactly = 0) { backupRepository.exportTo(any()) }
    }

    @Test
    fun `invoke with whitespace destination returns failure without calling repository`() = runTest {
        val result = useCase("   ")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_backup_destination)
        coVerify(exactly = 0) { backupRepository.exportTo(any()) }
    }

    @Test
    fun `invoke with destination calls exportTo and returns success`() = runTest {
        val destination = "content://downloads/JuguitoReader-backup.zip"
        coEvery { backupRepository.exportTo(destination) } returns Unit

        val result = useCase(destination)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { backupRepository.exportTo(destination) }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val destination = "content://downloads/JuguitoReader-backup.zip"
        val thrown = JuguitoException(R.string.error_backup_export)
        coEvery { backupRepository.exportTo(destination) } throws thrown

        val result = useCase(destination)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(thrown)
    }
}
