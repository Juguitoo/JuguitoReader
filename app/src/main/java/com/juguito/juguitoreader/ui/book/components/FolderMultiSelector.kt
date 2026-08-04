package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.domain.model.Folder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FolderMultiSelector(
    availableFolders: List<Folder>,
    selectedFolders: List<Folder>,
    onFoldersChanged: (List<Folder>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Carpetas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

        if (availableFolders.isEmpty()) {
            Text(
                "No tienes carpetas creadas. Puedes añadirlas desde el menú lateral.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFolders.forEach { folder ->
                    val isSelected = selectedFolders.any { it.name == folder.name }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSelection = if (isSelected) {
                                selectedFolders.filter { it.name != folder.name }
                            } else {
                                selectedFolders + folder
                            }
                            onFoldersChanged(newSelection)
                        },
                        label = { Text(folder.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(folder.colorHex.toColorInt()))
                            )
                        }
                    )
                }
            }
        }
    }
}