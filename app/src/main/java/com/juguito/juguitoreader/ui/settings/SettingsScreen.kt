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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.ui.common.toUiText
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
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
            SettingsSection(title = stringResource(R.string.appearance)) {
                SettingsSelectorRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.app_theme),
                    subtitle = state.appTheme.toUiText().asString(),
                    options = AppTheme.entries.map {
                        it to it.toUiText().asString()
                    },
                    selectedOption = state.appTheme,
                    onOptionSelected = { viewModel.onEvent(SettingsEvent.OnAppThemeChanged(it)) }
                )
            }

            SettingsSection(title = stringResource(R.string.reading_experience)) {
                SettingsSelectorRow(
                    icon = Icons.Default.AutoStories,
                    title = stringResource(R.string.reader_background),
                    subtitle = state.readerTheme.toUiText().asString(),
                    options = ReaderTheme.entries.map {
                        it to it.toUiText().asString()
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
                                text = stringResource(R.string.text_size),
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

            SettingsSection(title = stringResource(R.string.automations)) {
                SettingsSwitchRow(
                    icon = Icons.Default.BookmarkAdded,
                    title = stringResource(R.string.auto_start_title),
                    subtitle = stringResource(R.string.auto_start_subtitle),
                    checked = state.autoStart,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.OnAutoPendingToReadingChanged(it)) },
                    enabled = true
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    icon = Icons.Default.CheckCircleOutline,
                    title = stringResource(R.string.auto_finish_title),
                    subtitle = stringResource(R.string.auto_finish_subtitle),
                    checked = state.autoFinish,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.OnAutoFinishChanged(it)) },
                    enabled = true
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitchRow(
                    icon = Icons.Default.AutoStories,
                    title = stringResource(R.string.prompt_status_change_title),
                    subtitle = stringResource(R.string.prompt_status_change_subtitle),
                    checked = state.promptStatusChange,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.OnPromptStatusChangeChanged(it)) },
                    enabled = !state.autoStart
                )
            }
            SettingsSection(title = stringResource(R.string.general)) {
                SettingsSelectorRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = state.language.toUiText().asString(),
                    options = Language.entries.map {
                        it to it.toUiText().asString()
                    },
                    selectedOption = state.language,
                    onOptionSelected = { viewModel.onEvent(SettingsEvent.OnLanguageChanged(it)) }
                )
            }
        }
    }
}

