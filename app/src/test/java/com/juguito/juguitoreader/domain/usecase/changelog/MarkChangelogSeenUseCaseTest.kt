package com.juguito.juguitoreader.domain.usecase.changelog

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MarkChangelogSeenUseCaseTest {

    private lateinit var useCase: MarkChangelogSeenUseCase
    private val settingsRepository = mockk<SettingsRepository>()

    @Before
    fun setup() {
        useCase = MarkChangelogSeenUseCase(settingsRepository)
    }

    @Test
    fun `invoke saves current version as last seen`() = runTest {
        coEvery { settingsRepository.saveLastSeenChangelogVersion("1.2.1") } returns Unit

        useCase("1.2.1")

        coVerify { settingsRepository.saveLastSeenChangelogVersion("1.2.1") }
    }
}
