package com.juguito.juguitoreader.ui.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.components.JuguitoDialog

@Composable
fun WhatsNewDialog(
    onDismissRequest: () -> Unit,
    state: WhatsNewUiState,
) {
    val visible = state as? WhatsNewUiState.Visible ?: return
    val releases = ChangelogUiCatalog.newestFirst.filter { it.versionName in visible.versions }
    if (releases.isEmpty()) {
        LaunchedEffect(visible.versions) { onDismissRequest() }
        return
    }

    JuguitoDialog(
        onDismissRequest = onDismissRequest,
        icon = Icons.Default.NewReleases,
        title = stringResource(R.string.whats_new_title, visible.currentVersion),
        message = stringResource(R.string.whats_new_message),
        confirmButtonText = stringResource(R.string.whats_new_got_it),
        onConfirm = onDismissRequest,
        dismissButtonText = null,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                releases.forEach { release ->
                    val highlights = stringArrayResource(release.highlightsRes)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (releases.size > 1) {
                            Text(
                                text = stringResource(R.string.about_version, release.versionName),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        highlights.forEach { bullet ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(6.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = bullet,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
