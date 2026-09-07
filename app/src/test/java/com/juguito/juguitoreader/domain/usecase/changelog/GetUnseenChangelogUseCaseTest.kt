package com.juguito.juguitoreader.domain.usecase.changelog

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetUnseenChangelogUseCaseTest {

    private lateinit var useCase: GetUnseenChangelogUseCase
    private val settingsRepository = mockk<SettingsRepository>()
    private val catalog = listOf("1.2.1", "1.2.0")

    @Before
    fun setup() {
        useCase = GetUnseenChangelogUseCase(settingsRepository)
    }

    @Test
    fun `invoke with no last seen returns current when it is in catalog`() = runTest {
        every { settingsRepository.lastSeenChangelogVersion } returns flowOf(null)

        val result = useCase("1.2.1", catalog)

        assertThat(result).containsExactly("1.2.1")
    }

    @Test
    fun `invoke with last seen equal to current returns empty`() = runTest {
        every { settingsRepository.lastSeenChangelogVersion } returns flowOf("1.2.1")

        val result = useCase("1.2.1", catalog)

        assertThat(result).isEmpty()
    }

    @Test
    fun `invoke with older last seen returns unseen versions`() = runTest {
        every { settingsRepository.lastSeenChangelogVersion } returns flowOf("1.2.0")

        val result = useCase("1.2.1", catalog)

        assertThat(result).containsExactly("1.2.1")
    }
}
