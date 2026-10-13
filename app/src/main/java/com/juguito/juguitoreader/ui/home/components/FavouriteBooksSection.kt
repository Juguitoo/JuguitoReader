package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.book.components.formatRating
import java.util.Locale

@Composable
fun FavouriteBooksSection(
    books: List<Book>,
    onOpenBook: (Book) -> Unit,
    onReadBook: (Int) -> Unit,
    onBookDetails: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onViewCollection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (books.isEmpty()) return

    val gold = MaterialTheme.colorScheme.secondary
    val goldInk = lerp(gold, MaterialTheme.colorScheme.onSurface, 0.55f)
    val ranked = books
        .sortedWith(compareByDescending<Book> { it.rating }.thenBy { it.title.lowercase() })
        .take(3)
    val places = when (ranked.size) {
        3 -> listOf(2 to ranked[1], 1 to ranked[0], 3 to ranked[2])
        2 -> listOf(2 to ranked[1], 1 to ranked[0])
        else -> listOf(1 to ranked[0])
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 20.dp)) {
        HorizontalDivider(color = goldInk.copy(alpha = 0.28f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to gold.copy(alpha = 0.22f),
                        0.55f to gold.copy(alpha = 0.10f),
                        1f to Color.Transparent
                    )
                )
                .padding(top = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = gold.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(16.dp),
                        tint = goldInk
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_favourites_title),
                        modifier = Modifier.semantics { heading() },
                        style = homeSerif(size = 20.sp, lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.home_favourites_kicker)
                            .uppercase(LocalLocale.current.platformLocale),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                        color = goldInk
                    )
                }
                SectionLink(
                    label = stringResource(R.string.see_collection),
                    onClick = onViewCollection,
                    color = goldInk,
                    showChevron = true
                )
            }

            FavouritesPodium(
                places = places,
                gold = gold,
                onOpenBook = onOpenBook,
                onReadBook = onReadBook,
                onBookDetails = onBookDetails,
                onDeleteBook = onDeleteBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        HorizontalDivider(color = goldInk.copy(alpha = 0.28f))
    }
}

@Composable
private fun FavouritesPodium(
    places: List<Pair<Int, Book>>,
    gold: Color,
    onOpenBook: (Book) -> Unit,
    onReadBook: (Int) -> Unit,
    onBookDetails: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        places.forEach { (place, book) ->
            PodiumPlace(
                place = place,
                book = book,
                gold = gold,
                onOpen = { onOpenBook(book) },
                onRead = { onReadBook(book.id) },
                onDetails = { onBookDetails(book.id) },
                onDelete = { onDeleteBook(book) }
            )
        }
    }
}

@Composable
private fun PodiumPlace(
    place: Int,
    book: Book,
    gold: Color,
    onOpen: () -> Unit,
    onRead: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val coverWidth = when (place) {
        1 -> 104.dp
        2 -> 84.dp
        else -> 76.dp
    }
    val coverHeight = when (place) {
        1 -> 156.dp
        2 -> 126.dp
        else -> 114.dp
    }
    val stepHeight = when (place) {
        1 -> 40.dp
        2 -> 32.dp
        else -> 26.dp
    }
    val stepColor = when (place) {
        1 -> gold
        2 -> Color(0xFFC9CED6)
        else -> Color(0xFFC4845A)
    }
    val stepLabel = stringResource(
        when (place) {
            1 -> R.string.podium_gold
            2 -> R.string.podium_silver
            else -> R.string.podium_bronze
        }
    )
    val stepShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    val stepFill = lerp(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.onSurface,
        0.07f
    )
    val stepInk = lerp(stepColor, MaterialTheme.colorScheme.onSurface, 0.55f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(coverWidth)
                .height(coverHeight)
                .shadow(
                    elevation = 10.dp,
                    shape = RectangleShape,
                    ambientColor = Color.Black.copy(alpha = 0.28f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = { showMenu = true },
                    onLongClickLabel = stringResource(R.string.options),
                    role = Role.Button
                )
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape
            )
            CoverSpineScrim(modifier = Modifier.matchParentSize())
            if (book.rating > 0f) {
                RatingBadge(
                    rating = book.rating,
                    containerColor = gold,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(coverWidth + 20.dp)
                .height(stepHeight)
                .background(stepFill, stepShape)
                .drawBehind {
                    val radius = 12.dp.toPx()
                    val stroke = 1.5.dp.toPx()
                    val path = Path().apply {
                        moveTo(stroke / 2f, size.height)
                        lineTo(stroke / 2f, radius)
                        quadraticTo(stroke / 2f, stroke / 2f, radius, stroke / 2f)
                        lineTo(size.width - radius, stroke / 2f)
                        quadraticTo(
                            size.width - stroke / 2f,
                            stroke / 2f,
                            size.width - stroke / 2f,
                            radius
                        )
                        lineTo(size.width - stroke / 2f, size.height)
                    }
                    drawPath(
                        path = path,
                        color = stepColor,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepLabel,
                style = homeSans(size = 11.sp, fontWeight = FontWeight.Medium),
                color = stepInk,
                maxLines = 1
            )
        }
        BookHomeActionsMenu(
            expanded = showMenu,
            book = book,
            onDismiss = { showMenu = false },
            onRead = onRead,
            onDetails = onDetails,
            onDelete = onDelete
        )
    }
}

@Composable
private fun RatingBadge(
    rating: Float,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val label = formatRating(rating, Locale.getDefault())
    val description = stringResource(R.string.rating_out_of, label)

    Surface(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp))
            .clearAndSetSemantics { contentDescription = description },
        shape = RoundedCornerShape(4.dp),
        color = containerColor
    ) {
        Text(
            text = "★ $label",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = homeSans(size = 9.sp, fontWeight = FontWeight.Bold),
            color = contentColor,
            maxLines = 1
        )
    }
}
