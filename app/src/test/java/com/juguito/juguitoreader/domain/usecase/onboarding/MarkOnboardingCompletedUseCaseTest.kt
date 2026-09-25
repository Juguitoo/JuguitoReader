package com.juguito.juguitoreader.domain.usecase.onboarding

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkOnboardingCompletedUseCaseTest {

    private val settingsRepository = mockk<SettingsRepository>()
    private val useCase = MarkOnboardingCompletedUseCase(settingsRepository)

    @Test
    fun `invoke marks onboarding and changelog when last seen is missing`() = runTest {
        every { settingsRepository.lastSeenChangelogVersionFlow } returns flowOf(null)
        coEvery { settingsRepository.saveOnboardingCompleted(true) } returns Unit
        coEvery { settingsRepository.saveLastSeenChangelogVersion("1.2.2") } returns Unit

        useCase("1.2.2")

        coVerify { settingsRepository.saveOnboardingCompleted(true) }
        coVerify { settingsRepository.saveLastSeenChangelogVersion("1.2.2") }
    }

    @Test
    fun `invoke marks onboarding only when changelog was already seen`() = runTest {
        every { settingsRepository.lastSeenChangelogVersionFlow } returns flowOf("1.2.2")
        coEvery { settingsRepository.saveOnboardingCompleted(true) } returns Unit

        useCase("1.2.2")

        coVerify { settingsRepository.saveOnboardingCompleted(true) }
        coVerify(exactly = 0) { settingsRepository.saveLastSeenChangelogVersion(any()) }
    }
}
