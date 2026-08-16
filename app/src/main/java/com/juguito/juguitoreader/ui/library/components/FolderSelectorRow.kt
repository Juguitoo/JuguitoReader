package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.domain.model.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectorRow(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onExpandClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Ver todas las carpetas",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)

        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFolder == null,
                    onClick = { onFolderSelected(null) },
                    label = { Text("Todos") },
                    shape = CircleShape
                )
            }
            items(folders, key = { it.id }) { folder ->
                val isSelected = selectedFolder?.id == folder.id
                val color = Color(folder.colorHex.toColorInt())

                FilterChip(
                    selected = isSelected,
                    onClick = { onFolderSelected(folder) },
                    label = { Text(folder.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color,
                        selectedLeadingIconColor = color
                    )
                )
            }
        }
    }
}