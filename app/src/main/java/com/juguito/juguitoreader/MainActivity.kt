package com.juguito.juguitoreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juguito.juguitoreader.ui.navigation.JuguitoApp
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import com.juguito.juguitoreader.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            JuguitoReaderTheme(appTheme = currentTheme) {
                JuguitoApp()
            }
        }
    }
}
