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

class ImportBackupUseCaseTest {

    private lateinit var useCase: ImportBackupUseCase
    private val backupRepository = mockk<BackupRepository>()

    @Before
    fun setup() {
        useCase = ImportBackupUseCase(backupRepository)
    }

    @Test
    fun `invoke with blank source returns failure without calling repository`() = runTest {
        val result = useCase("")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_backup_source)
        coVerify(exactly = 0) { backupRepository.importFrom(any()) }
    }

    @Test
    fun `invoke with whitespace source returns failure without calling repository`() = runTest {
        val result = useCase("   ")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_backup_source)
        coVerify(exactly = 0) { backupRepository.importFrom(any()) }
    }

    @Test
    fun `invoke with source calls importFrom and returns success`() = runTest {
        val source = "content://downloads/JuguitoReader-backup.zip"
        coEvery { backupRepository.importFrom(source) } returns Unit

        val result = useCase(source)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { backupRepository.importFrom(source) }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val source = "content://downloads/JuguitoReader-backup.zip"
        val thrown = JuguitoException(R.string.error_backup_import_unsupported_format)
        coEvery { backupRepository.importFrom(source) } throws thrown

        val result = useCase(source)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(thrown)
    }
}
