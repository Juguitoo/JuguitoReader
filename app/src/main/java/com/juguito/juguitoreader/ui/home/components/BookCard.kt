package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.juguito.juguitoreader.domain.model.Book

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: Book,
    width: Dp,
    onBookDetails: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onReadBook: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // 1. Convertimos la raíz en un Box para poder superponer las "páginas" debajo de la portada
    Box(
        modifier = Modifier
            .width(width)
            // Dejamos margen derecho e inferior para que el efecto de grosor no se corte
            .padding(end = 6.dp)
            .combinedClickable(
                onClick = { onReadBook(book.id) },
                onLongClick = { showMenu = true }
            )
    ) {

        // 2. CAPA INFERIOR: El grosor del libro (Las páginas)
        // Usamos matchParentSize() para que mida exactamente lo mismo que la portada
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 0.dp) // Lo desplazamos a la derecha y abajo
                .background(Color(0xFFD6D1C4)) // Color marfil/grisáceo de hojas de papel
                .shadow(elevation = 2.dp, shape = RectangleShape) // Sombrita leve sobre la madera
        )

        // 3. CAPA SUPERIOR: La Portada (Tu Card original)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            // Quitamos la elevación aquí para que esté pegada a las "páginas"
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.65f)
                ) {
                    if (book.coverUrl != null) {
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // EFECTO PRO: Sombra curva en el lomo izquierdo del libro
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f), // Sombra más dura a la izquierda
                                        Color.Transparent, // Se difumina rápido
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 150f // Distancia a la que desaparece la sombra
                                )
                            )
                    )

                    // EL MARCAPÁGINAS
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = (-14).dp, x = 8.dp)
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable { showMenu = true }
                            .padding(all = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(18.dp)
                                .offset(y = (-2).dp)
                        )
                    }

                    // MENÚ OVERLAY
                    if (showMenu) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f))
                                .clickable { showMenu = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MenuOverlayItem(
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    size = 40,
                                    onClick = {
                                        showMenu = false
                                        onReadBook(book.id)
                                    }
                                )
                                MenuOverlayItem(
                                    icon = Icons.Default.Info,
                                    size = 40,
                                    onClick = {
                                        showMenu = false
                                        onBookDetails(book.id)
                                    }
                                )
                                MenuOverlayItem(
                                    icon = Icons.Default.Delete,
                                    size = 40,
                                    color = MaterialTheme.colorScheme.error,
                                    onClick = {
                                        showMenu = false
                                        onDeleteBook(book)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}