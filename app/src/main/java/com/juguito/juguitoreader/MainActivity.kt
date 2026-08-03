package com.juguito.juguitoreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.juguito.juguitoreader.ui.navigation.JuguitoApp
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JuguitoReaderTheme {
                JuguitoApp()
            }
        }
    }
}