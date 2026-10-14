package com.juguito.juguitoreader.ui.library.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.juguito.juguitoreader.domain.model.Folder

@Composable
fun FolderSelectorRow(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onFolderSelected: (Folder?) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            ShelfBackdrop(Modifier.matchParentSize())
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                items(folders, key = { it.id }) { folder ->
                    FolderBinder(
                        selected = selectedFolder?.id == folder.id,
                        folder = folder,
                        onClick = { onFolderSelected(folder) }
                    )
                }
            }
        }
        ShelfPlank()
    }
}

@Composable
private fun ShelfBackdrop(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val top = if (dark) Color(0xFF3E2723) else Color(0xFF8D6E63)
    val bottom = if (dark) Color(0xFF4E342E) else Color(0xFFA1887F)
    val grain = if (dark) Color(0xFF2B1B17) else Color(0xFF6D4C41)

    Box(
        modifier.drawBehind {
            drawRect(brush = Brush.verticalGradient(listOf(top, bottom)))
            val line = grain.copy(alpha = 0.28f)
            val step = 7.dp.toPx()
            var y = step
            while (y < size.height) {
                drawLine(
                    color = line,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += step
            }
        }
    )
}

@Composable
private fun ShelfPlank(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val face = if (dark) Color(0xFF4E342E) else Color(0xFFA0724A)
    val lip = if (dark) Color(0xFF6D4C41) else Color(0xFFC4A574)
    val grain = if (dark) Color(0xFF3E2723) else Color(0xFF6D4C41)
    val shadow = Color.Black.copy(alpha = if (dark) 0.45f else 0.22f)

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .drawBehind {
                    drawRect(face)
                    drawRect(color = lip, size = Size(size.width, 2.dp.toPx()))
                    val line = grain.copy(alpha = 0.35f)
                    listOf(0.4f, 0.62f, 0.84f).forEach { fraction ->
                        drawLine(
                            color = line,
                            start = Offset(0f, size.height * fraction),
                            end = Offset(size.width, size.height * fraction),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    val shadowHeight = 4.dp.toPx()
                    drawRect(
                        brush = Brush.verticalGradient(listOf(shadow, Color.Transparent)),
                        topLeft = Offset(0f, size.height),
                        size = Size(size.width, shadowHeight)
                    )
                }
        )
    }
}