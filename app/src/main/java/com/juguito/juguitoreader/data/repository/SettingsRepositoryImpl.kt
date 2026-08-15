package com.juguito.juguitoreader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private companion object {
        val TEXT_ZOOM = intPreferencesKey("text_zoom")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val APP_THEME = stringPreferencesKey("app_theme")
        val READER_BRIGHTNESS = floatPreferencesKey("reader_brightness")
    }

    override val textZoomFlow: Flow<Int> = context.dataStore.data.map { it[TEXT_ZOOM] ?: 100 }
    override val readerThemeFlow: Flow<String> = context.dataStore.data.map { it[READER_THEME] ?: "SEPIA" }
    override val appThemeFlow: Flow<String> = context.dataStore.data.map { it[APP_THEME] ?: "JUGUITO" }
    override val readerBrightnessFlow: Flow<Float> = context.dataStore.data.map { it[READER_BRIGHTNESS] ?: 0.5f }

    override suspend fun saveTextZoom(textZoom: Int) {
        context.dataStore.edit { it[TEXT_ZOOM] = textZoom }
    }

    override suspend fun saveReaderTheme(readerTheme: String) {
        context.dataStore.edit { it[READER_THEME] = readerTheme }
    }

    override suspend fun saveAppTheme(appTheme: String) {
        context.dataStore.edit { it[APP_THEME] = appTheme }
    }

    override suspend fun saveReaderBrightness(readerBrightness: Float) {
        context.dataStore.edit { it[READER_BRIGHTNESS] = readerBrightness }
    }
}