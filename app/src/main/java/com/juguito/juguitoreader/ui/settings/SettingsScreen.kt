package com.juguito.juguitoreader.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.settings.components.ReaderPreviewBox
import com.juguito.juguitoreader.ui.settings.components.SettingsSection
import com.juguito.juguitoreader.ui.settings.components.SettingsSelectorRow
import com.juguito.juguitoreader.ui.settings.components.SettingsSwitchRow
import com.juguito.juguitoreader.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Apariencia") {
                SettingsSelectorRow(
                    icon = Icons.Default.Palette,
                    title = "Tema de la aplicación",
                    subtitle = state.appTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                    options = AppTheme.entries.map {
                        it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                    },
                    selectedOption = state.appTheme,
                    onOptionSelected = { viewModel.onEvent(SettingsEvent.OnAppThemeChanged(it)) }
                )
            }

            SettingsSection(title = "Experiencia de lectura") {
                SettingsSelectorRow(
                    icon = Icons.Default.AutoStories,
                    title = "Fondo del lector",
                    subtitle = state.readerTheme.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    options = ReaderTheme.entries.map {
                        it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                    },
                    selectedOption = state.readerTheme,
                    onOptionSelected = { viewModel.onEvent(SettingsEvent.OnReaderThemeChanged(it)) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tamaño de texto",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${state.textZoom}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = state.textZoom.toFloat(),
                        onValueChange = { viewModel.onEvent(SettingsEvent.OnTextZoomChanged(it.toInt())) },
                        valueRange = 50f..200f,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        steps = 12,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        },
                        track = { positions ->
                            SliderDefaults.Track(
                                colors = SliderDefaults.colors(
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                sliderState = positions,
                                modifier = Modifier.height(2.dp)
                            )
                        }
                    )

                    ReaderPreviewBox(
                        theme = state.readerTheme,
                        textSizePercentage = state.textZoom
                    )
                }
            }

            SettingsSection(title = "Automatizaciones") {
                SettingsSwitchRow(
                    icon = Icons.Default.BookmarkAdded,
                    title = "Estado 'Leyendo'",
                    subtitle = "Mueve el libro de 'Pendiente' a 'Leyendo' al abrirlo.",
                    checked = state.autoStart,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.OnAutoPendingToReadingChanged(it)) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    icon = Icons.Default.CheckCircleOutline,
                    title = "Marcar como 'Terminado'",
                    subtitle = "Completa el libro al llegar al 100% de la lectura.",
                    checked = state.autoFinish,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.OnAutoFinishChanged(it)) }
                )
            }
/*
            SettingsSection(title = "General") {
                SettingsSelectorRow(
                    icon = Icons.Default.Language,
                    title = "Idioma",
                    subtitle = state.language.displayName.lowercase().replaceFirstChar { it.uppercase() },
                    options = Language.entries.map { it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    selectedOption = state.language,
                    onOptionSelected = { viewModel.onEvent(SettingsEvent.OnLanguageChanged(it)) }
                )
            }

 */
        }
    }
}

