package com.juguito.juguitoreader.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.ReadingGenreStat
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.ui.book.components.formatRating
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalLocale

@Composable
internal fun StatsInsights(stats: ReadingStats) {
    val showAllTime = stats.mostAllReadGenre != null && stats.totalFinished > 0
    val showPeriod = stats.mostReadGenreInPeriod != null && stats.finishedInPeriod.isNotEmpty()
    val rating = stats.averageFinishedRating
    if (!showAllTime && !showPeriod && rating == null) return
    val locale = LocalLocale.current.platformLocale

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = statsCardBorder(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.stats_insights),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = LoraFontFamily,
                )
            }
            GenreInsight(stats = stats)
            if (rating != null) {
                InsightRow(
                    icon = Icons.Default.Star,
                    tint = MaterialTheme.colorScheme.secondary,
                    label = stringResource(R.string.stats_average_rating),
                    value = formatRating(rating, locale),
                    detail = stringResource(R.string.stats_average_rating_detail),
                )
            }
        }
    }
}

@Composable
private fun GenreInsight(stats: ReadingStats) {
    val allTime = stats.mostAllReadGenre
    if (allTime != null && stats.totalFinished > 0) {
        InsightRow(
            icon = Icons.Default.Category,
            tint = MaterialTheme.colorScheme.primary,
            label = stringResource(R.string.stats_genre),
            value = allTime.genre.name,
            detail = stringResource(
                R.string.stats_genre_share,
                genrePercent(allTime, stats.totalFinished),
            ),
        )
    }
    val inPeriod = stats.mostReadGenreInPeriod
    if (inPeriod != null && stats.finishedInPeriod.isNotEmpty()) {
        InsightRow(
            icon = Icons.Default.Category,
            tint = MaterialTheme.colorScheme.secondary,
            label = stringResource(R.string.stats_genre_period),
            value = inPeriod.genre.name,
            detail = stringResource(
                R.string.stats_genre_share_period,
                genrePercent(inPeriod, stats.finishedInPeriod.size),
            ),
        )
    }
}

@Composable
private fun InsightRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(statsInsetColor())
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = tint,
            )
        }
    }
}

private fun genrePercent(stat: ReadingGenreStat, total: Int): Int {
    if (total <= 0) return 0
    return (stat.genreFinished * 100f / total).roundToInt()
}
