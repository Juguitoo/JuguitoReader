package com.juguito.juguitoreader.ui.changelog

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.usecase.changelog.GetUnseenChangelogUseCase
import com.juguito.juguitoreader.domain.usecase.changelog.MarkChangelogSeenUseCase
import com.juguito.juguitoreader.utils.appVersionName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
class WhatsNewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val getUnseenChangelogUseCase = mockk<GetUnseenChangelogUseCase>()
    private val markChangelogSeenUseCase = mockk<MarkChangelogSeenUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("com.juguito.juguitoreader.utils.AppVersionNameKt")
        every { context.appVersionName() } returns "1.2.1"
        coEvery { markChangelogSeenUseCase(any()) } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() = WhatsNewViewModel(
        context,
        getUnseenChangelogUseCase,
        markChangelogSeenUseCase
    )

    @Test
    fun `stays idle when last seen equals current`() = runTest {
        coEvery { getUnseenChangelogUseCase(any(), any()) } returns emptyList()

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(WhatsNewUiState.Idle)
            expectNoEvents()
        }
    }

    @Test
    fun `shows dialog when last seen is missing and current is in catalog`() = runTest {
        coEvery { getUnseenChangelogUseCase("1.2.1", any()) } returns listOf("1.2.1")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(
                WhatsNewUiState.Visible(currentVersion = "1.2.1", versions = listOf("1.2.1"))
            )
        }
        coVerify {
            getUnseenChangelogUseCase(
                "1.2.1",
                ChangelogUiCatalog.newestFirst.map { it.versionName }
            )
        }
    }

    @Test
    fun `dismiss persists current version and returns to idle`() = runTest {
        coEvery { getUnseenChangelogUseCase(any(), any()) } returns listOf("1.2.1")
        val viewModel = createViewModel()

        viewModel.onEvent(WhatsNewEvent.OnDismiss)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(WhatsNewUiState.Idle)
        }
        coVerify { markChangelogSeenUseCase("1.2.1") }
    }

    @Test
    fun `dismiss while idle does not mark changelog seen`() = runTest {
        coEvery { getUnseenChangelogUseCase(any(), any()) } returns emptyList()
        val viewModel = createViewModel()

        viewModel.onEvent(WhatsNewEvent.OnDismiss)

        coVerify(exactly = 0) { markChangelogSeenUseCase(any()) }
    }

    @Test
    fun `stays idle when loading unseen versions fails`() = runTest {
        coEvery { getUnseenChangelogUseCase(any(), any()) } throws IllegalStateException("datastore")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(WhatsNewUiState.Idle)
            expectNoEvents()
        }
    }
}
