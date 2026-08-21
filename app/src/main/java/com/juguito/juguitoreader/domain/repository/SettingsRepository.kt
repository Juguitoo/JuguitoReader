package com.juguito.juguitoreader.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val textZoomFlow: Flow<Int>
    val readerThemeFlow: Flow<String>
    val appThemeFlow: Flow<String>
    val readerBrightnessFlow: Flow<Float>
    val languageFlow: Flow<String>
    val autoStartReadingFlow: Flow<Boolean>
    val autoFinishReadingFlow: Flow<Boolean>
    val promptStatusChangeFlow: Flow<Boolean>

    suspend fun saveTextZoom(textZoom: Int)
    suspend fun saveReaderTheme(readerTheme: String)
    suspend fun saveAppTheme(appTheme: String)
    suspend fun saveReaderBrightness(readerBrightness: Float)
    suspend fun saveLanguage(language: String)
    suspend fun saveAutoStartReading(enable: Boolean)
    suspend fun saveAutoFinishReading(enable: Boolean)
    suspend fun savePromptStatusChange(enable: Boolean)
}