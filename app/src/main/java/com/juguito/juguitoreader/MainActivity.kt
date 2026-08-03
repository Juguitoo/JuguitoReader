package com.juguito.juguitoreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.juguito.juguitoreader.ui.navigation.JuguitoApp
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import com.juguito.juguitoreader.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            
            JuguitoReaderTheme(appTheme = currentTheme) {
                JuguitoApp()
            }
        }
    }
}
