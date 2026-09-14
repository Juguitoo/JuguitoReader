package com.juguito.juguitoreader.ui.settings.backup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.usecase.backup.ExportBackupUseCase
import com.juguito.juguitoreader.domain.usecase.backup.ImportBackupUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val exportBackupUseCase = mockk<ExportBackupUseCase>()
    private val importBackupUseCase = mockk<ImportBackupUseCase>()
    private lateinit var viewModel: BackupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BackupViewModel(exportBackupUseCase, importBackupUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
    }

    @Test
    fun `OnRestoreClick shows confirm dialog`() = runTest {
        viewModel.onEvent(BackupEvent.OnRestoreClick)

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.ConfirmImport)
        coVerify(exactly = 0) { importBackupUseCase(any()) }
    }

    @Test
    fun `OnCancelRestore returns to idle`() = runTest {
        viewModel.onEvent(BackupEvent.OnRestoreClick)
        viewModel.onEvent(BackupEvent.OnCancelRestore)

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
        coVerify(exactly = 0) { importBackupUseCase(any()) }
    }

    @Test
    fun `OnConfirmRestore returns to idle without importing`() = runTest {
        viewModel.onEvent(BackupEvent.OnRestoreClick)
        viewModel.onEvent(BackupEvent.OnConfirmRestore)

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
        coVerify(exactly = 0) { importBackupUseCase(any()) }
    }

    @Test
    fun `OnExportPicked success shows snackbar and returns to idle`() = runTest {
        val uri = "content://downloads/JuguitoReader-backup.zip"
        coEvery { exportBackupUseCase(uri) } returns Result.success(Unit)

        viewModel.onEvent(BackupEvent.OnExportPicked(uri))

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            val message = effect.message as UiText.StringResource
            assertThat(message.resId).isEqualTo(R.string.backup_export_success)
        }
        coVerify(exactly = 1) { exportBackupUseCase(uri) }
    }

    @Test
    fun `OnExportPicked failure shows asUiText snackbar and returns to idle`() = runTest {
        val uri = "content://downloads/JuguitoReader-backup.zip"
        val error = JuguitoException(R.string.error_backup_export)
        coEvery { exportBackupUseCase(uri) } returns Result.failure(error)

        viewModel.onEvent(BackupEvent.OnExportPicked(uri))

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            val message = effect.message as UiText.StringResource
            assertThat(message.resId).isEqualTo(R.string.error_backup_export)
        }
    }

    @Test
    fun `OnImportPicked success emits RestartApp and stays importing`() = runTest {
        val uri = "content://downloads/JuguitoReader-backup.zip"
        coEvery { importBackupUseCase(uri) } returns Result.success(Unit)

        viewModel.onEvent(BackupEvent.OnImportPicked(uri))

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Importing)
        viewModel.effect.test {
            assertThat(awaitItem()).isEqualTo(UiEffect.RestartApp)
        }
        coVerify(exactly = 1) { importBackupUseCase(uri) }
    }

    @Test
    fun `OnImportPicked failure shows asUiText snackbar and returns to idle`() = runTest {
        val uri = "content://downloads/JuguitoReader-backup.zip"
        val error = JuguitoException(R.string.error_backup_import_unsupported_format)
        coEvery { importBackupUseCase(uri) } returns Result.failure(error)

        viewModel.onEvent(BackupEvent.OnImportPicked(uri))

        assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Idle)
        viewModel.effect.test {
            val effect = awaitItem() as UiEffect.ShowSnackbar
            val message = effect.message as UiText.StringResource
            assertThat(message.resId).isEqualTo(R.string.error_backup_import_unsupported_format)
        }
    }

    @Test
    fun `events while exporting are ignored`() = runTest {
        val firstUri = "content://downloads/first.zip"
        val secondUri = "content://downloads/second.zip"
        val latch = CompletableDeferred<Result<Unit>>()
        coEvery { exportBackupUseCase(firstUri) } coAnswers { latch.await() }

        try {
            viewModel.onEvent(BackupEvent.OnExportPicked(firstUri))
            viewModel.onEvent(BackupEvent.OnExportPicked(secondUri))
            viewModel.onEvent(BackupEvent.OnRestoreClick)
            viewModel.onEvent(BackupEvent.OnImportPicked(secondUri))

            assertThat(viewModel.uiState.value).isEqualTo(BackupUiState.Exporting)
            coVerify(exactly = 1) { exportBackupUseCase(firstUri) }
            coVerify(exactly = 0) { exportBackupUseCase(secondUri) }
            coVerify(exactly = 0) { importBackupUseCase(any()) }
        } finally {
            latch.complete(Result.success(Unit))
        }
    }
}
