package com.juguito.juguitoreader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl
    private val context = mockk<Context>()

    @Before
    fun setup() {
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "settings.preferences_pb") }
        )

        mockkStatic("com.juguito.juguitoreader.data.repository.SettingsRepositoryImplKt")
        every { context.dataStore } returns testDataStore
        
        repository = SettingsRepositoryImpl(context)
    }

    @Test
    fun `textZoomFlow returns default value when empty`() = runTest {
        assertThat(repository.textZoomFlow.first()).isEqualTo(100)
    }

    @Test
    fun `saveTextZoom updates the flow`() = runTest {
        repository.saveTextZoom(150)
        assertThat(repository.textZoomFlow.first()).isEqualTo(150)
    }

    @Test
    fun `readerThemeFlow returns default value when empty`() = runTest {
        assertThat(repository.readerThemeFlow.first()).isEqualTo("SEPIA")
    }

    @Test
    fun `saveReaderTheme updates the flow`() = runTest {
        repository.saveReaderTheme("NIGHT")
        assertThat(repository.readerThemeFlow.first()).isEqualTo("NIGHT")
    }

    @Test
    fun `appThemeFlow returns default value when empty`() = runTest {
        assertThat(repository.appThemeFlow.first()).isEqualTo("JUGUITO")
    }

    @Test
    fun `saveAppTheme updates the flow`() = runTest {
        repository.saveAppTheme("NEON")
        assertThat(repository.appThemeFlow.first()).isEqualTo("NEON")
    }

    @Test
    fun `readerBrightnessFlow returns default value when empty`() = runTest {
        assertThat(repository.readerBrightnessFlow.first()).isEqualTo(0.5f)
    }

    @Test
    fun `saveReaderBrightness updates the flow`() = runTest {
        repository.saveReaderBrightness(0.8f)
        assertThat(repository.readerBrightnessFlow.first()).isEqualTo(0.8f)
    }

    @Test
    fun `languageFlow returns default value when empty`() = runTest {
        assertThat(repository.languageFlow.first()).isEqualTo("SYSTEM")
    }

    @Test
    fun `saveLanguage updates the flow`() = runTest {
        repository.saveLanguage("ENGLISH")
        assertThat(repository.languageFlow.first()).isEqualTo("ENGLISH")
    }

    @Test
    fun `lastSeenChangelogVersion returns null when empty`() = runTest {
        assertThat(repository.lastSeenChangelogVersion.first()).isNull()
    }

    @Test
    fun `saveLastSeenChangelogVersion updates the flow`() = runTest {
        repository.saveLastSeenChangelogVersion("1.2.1")
        assertThat(repository.lastSeenChangelogVersion.first()).isEqualTo("1.2.1")
    }
}
