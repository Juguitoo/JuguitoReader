package com.juguito.juguitoreader.ui.reader

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.usecase.reader.GetReaderGuideCompletedUseCase
import com.juguito.juguitoreader.domain.usecase.reader.MarkReaderGuideCompletedUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class ReaderGuideViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getReaderGuideCompletedUseCase = mockk<GetReaderGuideCompletedUseCase>()
    private val markReaderGuideCompletedUseCase = mockk<MarkReaderGuideCompletedUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { markReaderGuideCompletedUseCase() } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ReaderGuideViewModel(
        getReaderGuideCompletedUseCase,
        markReaderGuideCompletedUseCase
    )

    @Test
    fun `shows guide when it has not been completed`() = runTest {
        coEvery { getReaderGuideCompletedUseCase() } returns false

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ReaderGuideUiState.Visible)
        }
    }

    @Test
    fun `hides guide when it is already completed`() = runTest {
        coEvery { getReaderGuideCompletedUseCase() } returns true

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ReaderGuideUiState.Hidden)
        }
    }

    @Test
    fun `dismiss hides guide and persists completion`() = runTest {
        coEvery { getReaderGuideCompletedUseCase() } returns false
        val viewModel = createViewModel()

        viewModel.onEvent(ReaderGuideEvent.OnDismiss)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ReaderGuideUiState.Hidden)
        }
        coVerify { markReaderGuideCompletedUseCase() }
    }

    @Test
    fun `dismiss while hidden does not persist`() = runTest {
        coEvery { getReaderGuideCompletedUseCase() } returns true
        val viewModel = createViewModel()

        viewModel.onEvent(ReaderGuideEvent.OnDismiss)

        coVerify(exactly = 0) { markReaderGuideCompletedUseCase() }
    }
}
