package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.common.components.ActionBookCard

@Composable
fun BookListSection(
    title: String,
    books: List<Book>,
    onBookDetails: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onReadBook: (Int) -> Unit,
    showActions: Boolean = false,
    onNavigateToAddBook: (() -> Unit)? = null,
    onImportClick: (() -> Unit)? = null
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val screenWidth = with(density) { containerSize.width.toDp() }
    val calculatedWidth = (screenWidth - 40.dp - 32.dp) / 3
    val itemWidth = calculatedWidth.coerceAtMost(120.dp)

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {

        Surface(
            color = MaterialTheme.colorScheme.secondary,
            shape = BookmarkShape(),
            shadowElevation = 8.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 24.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .paint(
                    painter = painterResource(id = R.drawable.madera_estante),
                    contentScale = ContentScale.FillBounds,
                    sizeToIntrinsics = false
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            LazyRow(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        width = itemWidth,
                        onBookDetails = onBookDetails,
                        onDeleteBook = onDeleteBook,
                        onReadBook = onReadBook
                    )
                }

                if (showActions) {
                    item {
                        ActionBookCard(
                            title = "Nuevo",
                            icon = Icons.Default.Add,
                            width = itemWidth,
                            onClick = { onNavigateToAddBook?.invoke() }
                        )
                    }
                    item {
                        ActionBookCard(
                            title = "Importar",
                            icon = Icons.Default.UploadFile,
                            width = itemWidth,
                            onClick = { onImportClick?.invoke() }
                        )
                    }
                }
            }
/*
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

 */
        }
    }
}