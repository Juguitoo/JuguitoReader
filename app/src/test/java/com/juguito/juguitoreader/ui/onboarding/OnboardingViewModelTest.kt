package com.juguito.juguitoreader.ui.onboarding

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.usecase.onboarding.GetOnboardingCompletedUseCase
import com.juguito.juguitoreader.domain.usecase.onboarding.MarkOnboardingCompletedUseCase
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
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val getOnboardingCompletedUseCase = mockk<GetOnboardingCompletedUseCase>()
    private val markOnboardingCompletedUseCase = mockk<MarkOnboardingCompletedUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("com.juguito.juguitoreader.utils.AppVersionNameKt")
        every { context.appVersionName() } returns "1.2.2"
        coEvery { markOnboardingCompletedUseCase(any()) } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() = OnboardingViewModel(
        context,
        getOnboardingCompletedUseCase,
        markOnboardingCompletedUseCase
    )

    @Test
    fun `shows onboarding when it has not been completed`() = runTest {
        coEvery { getOnboardingCompletedUseCase() } returns false

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(OnboardingUiState.Visible)
        }
    }

    @Test
    fun `hides onboarding when it is already completed`() = runTest {
        coEvery { getOnboardingCompletedUseCase() } returns true

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(OnboardingUiState.Hidden)
        }
    }

    @Test
    fun `finish hides onboarding and persists completion`() = runTest {
        coEvery { getOnboardingCompletedUseCase() } returns false
        val viewModel = createViewModel()

        viewModel.onEvent(OnboardingEvent.OnFinished)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(OnboardingUiState.Hidden)
        }
        coVerify { markOnboardingCompletedUseCase("1.2.2") }
    }

    @Test
    fun `finish while hidden does not persist`() = runTest {
        coEvery { getOnboardingCompletedUseCase() } returns true
        val viewModel = createViewModel()

        viewModel.onEvent(OnboardingEvent.OnFinished)

        coVerify(exactly = 0) { markOnboardingCompletedUseCase(any()) }
    }
}
