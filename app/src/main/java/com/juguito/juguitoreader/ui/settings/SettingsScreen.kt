package com.juguito.juguitoreader.ui.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.common.toUiText
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.settings.backup.BackupEvent
import com.juguito.juguitoreader.ui.settings.backup.BackupUiState
import com.juguito.juguitoreader.ui.settings.backup.BackupViewModel
import com.juguito.juguitoreader.ui.settings.backup.ProcessAppRestarter
import com.juguito.juguitoreader.ui.settings.components.ReaderPreviewBox
import com.juguito.juguitoreader.ui.settings.components.SettingsActionRow
import com.juguito.juguitoreader.ui.settings.components.SettingsSection
import com.juguito.juguitoreader.ui.settings.components.SettingsSelectorRow
import com.juguito.juguitoreader.ui.settings.components.SettingsSwitchRow
import com.juguito.juguitoreader.ui.theme.AppTheme
import com.juguito.juguitoreader.utils.openBugReport
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val backupState by backupViewModel.uiState.collectAsState()
    val isWorking = backupState is BackupUiState.Exporting ||
        backupState is BackupUiState.Importing
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val overlayScrimInteraction = remember { MutableInteractionSource() }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { backupViewModel.onEvent(BackupEvent.OnExportPicked(it.toString())) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { backupViewModel.onEvent(BackupEvent.OnImportPicked(it.toString())) }
    }

    ObserveAsEvents(backupViewModel.effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(uiEffect.message.asString(context))
            }
            is UiEffect.RestartApp -> {
                ProcessAppRestarter(context).restart()
            }
            else -> Unit
        }
    }

    BackHandler(enabled = isWorking) { }

    if (backupState is BackupUiState.ConfirmImport) {
        JuguitoDialog(
            onDismissRequest = { backupViewModel.onEvent(BackupEvent.OnCancelRestore) },
            icon = Icons.Default.Warning,
            title = stringResource(R.string.backup_restore_confirm_title),
            message = stringResource(R.string.backup_restore_confirm_message),
            confirmButtonText = stringResource(R.string.backup_restore_confirm_action),
            isDestructive = true,
            onConfirm = {
                backupViewModel.onEvent(BackupEvent.OnConfirmRestore)
                openDocumentLauncher.launch(
                    arrayOf("application/zip", "application/octet-stream")
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !isWorking
                    ) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                SettingsSection(title = stringResource(R.string.backup_section_title)) {
                    SettingsActionRow(
                        icon = Icons.Default.UploadFile,
                        title = stringResource(R.string.backup_export_title),
                        subtitle = stringResource(R.string.backup_export_subtitle),
                        enabled = !isWorking,
                        onClick = {
                            val fileName = "JuguitoReader-backup-${
                                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                            }.zip"
                            createDocumentLauncher.launch(fileName)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SettingsActionRow(
                        icon = Icons.Default.Download,
                        title = stringResource(R.string.backup_restore_title),
                        subtitle = stringResource(R.string.backup_restore_subtitle),
                        enabled = !isWorking,
                        onClick = { backupViewModel.onEvent(BackupEvent.OnRestoreClick) }
                    )
                }

                SettingsSection(title = stringResource(R.string.support_section_title)) {
                    SettingsActionRow(
                        icon = Icons.Default.BugReport,
                        title = stringResource(R.string.bug_report_title),
                        subtitle = stringResource(R.string.bug_report_subtitle),
                        enabled = !isWorking,
                        onClick = {
                            if (!context.openBugReport()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.bug_report_no_email_app)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            if (isWorking) {
                val overlayMessage = when (backupState) {
                    BackupUiState.Exporting -> R.string.backup_exporting
                    BackupUiState.Importing -> R.string.backup_importing
                    else -> null
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = overlayScrimInteraction,
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        overlayMessage?.let {
                            Text(
                                text = stringResource(it),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
