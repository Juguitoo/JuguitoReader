package com.juguito.juguitoreader.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val textZoomFlow: Flow<Int>
    val readerThemeFlow: Flow<String>
    val appThemeFlow: Flow<String>
    val readerBrightnessFlow: Flow<Float>

    suspend fun saveTextZoom(textZoom: Int)
    suspend fun saveReaderTheme(readerTheme: String)
    suspend fun saveAppTheme(appTheme: String)
    suspend fun saveReaderBrightness(readerBrightness: Float)
}