package com.juguito.juguitoreader.domain.usecase.onboarding

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetOnboardingCompletedUseCaseTest {

    private val settingsRepository = mockk<SettingsRepository>()
    private val useCase = GetOnboardingCompletedUseCase(settingsRepository)

    @Test
    fun `invoke returns false when onboarding is pending`() = runTest {
        every { settingsRepository.onboardingCompletedFlow } returns flowOf(false)

        assertThat(useCase()).isFalse()
    }

    @Test
    fun `invoke returns true when onboarding is completed`() = runTest {
        every { settingsRepository.onboardingCompletedFlow } returns flowOf(true)

        assertThat(useCase()).isTrue()
    }
}
