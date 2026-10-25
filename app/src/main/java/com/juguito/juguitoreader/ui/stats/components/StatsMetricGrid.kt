package com.juguito.juguitoreader.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.ui.theme.LoraFontFamily

@Composable
internal fun StatsMetricGrid(stats: ReadingStats) {
    val trend = readingTimeTrend(stats.readingTimeComparison)
    val pagesPerHour = pagesPerHour(stats.readingAverageVelocity)
    EqualHeightGrid(columns = 2, spacing = 12.dp) {
        StatsMetricCard(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(R.string.stats_streak),
            value = stats.readingStreak.toString(),
            unit = pluralStringResource(R.plurals.stats_day_unit, stats.readingStreak),
            valueColor = if (stats.readingStreak > 0) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            caption = stringResource(R.string.stats_record, stats.bestReadingStreak) +
                " " + pluralStringResource(R.plurals.stats_day_unit, stats.bestReadingStreak),
            captionIcon = Icons.Default.MilitaryTech,
            icon = Icons.Default.LocalFireDepartment,
            accent = MaterialTheme.colorScheme.secondary,
        )
        StatsMetricCard(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(R.string.stats_time_read),
            value = formatReadingMinutes(stats.readingTimeComparison.currentMinutes),
            unit = null,
            caption = trend?.label,
            captionIcon = trend?.let { trendIcon(it.trend) },
            captionColor = trendColor(trend?.trend),
            icon = Icons.Default.Schedule,
            accent = MaterialTheme.colorScheme.primary,
        )
        StatsMetricCard(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(R.string.stats_finished),
            value = stats.finishedInPeriod.size.toString(),
            unit = pluralStringResource(R.plurals.stats_book_unit, stats.finishedInPeriod.size),
            caption = stringResource(R.string.stats_finished_total, stats.totalFinished),
            captionIcon = Icons.Default.Flag,
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = MaterialTheme.colorScheme.primary,
        )
        StatsMetricCard(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(R.string.stats_speed),
            value = stats.readingAverageVelocity.toString(),
            unit = stringResource(R.string.stats_wpm),
            caption = if (pagesPerHour > 0) {
                stringResource(R.string.stats_pages_per_hour, pagesPerHour)
            } else {
                null
            },
            icon = Icons.Default.Speed,
            accent = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun StatsMetricCard(
    label: String,
    value: String,
    unit: String?,
    caption: String?,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    captionIcon: ImageVector? = null,
    captionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = statsCardBorder(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = LoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
            if (caption != null) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (captionIcon != null) {
                        Icon(
                            captionIcon,
                            contentDescription = null,
                            tint = captionColor,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.labelSmall,
                        color = captionColor,
                    )
                }
            }
        }
    }
}

private fun trendIcon(trend: ReadingTimeTrend): ImageVector {
    return when (trend) {
        ReadingTimeTrend.Up -> Icons.AutoMirrored.Filled.TrendingUp
        ReadingTimeTrend.Down -> Icons.AutoMirrored.Filled.TrendingDown
        ReadingTimeTrend.Flat -> Icons.AutoMirrored.Filled.TrendingFlat
    }
}

@Composable
private fun trendColor(trend: ReadingTimeTrend?): Color {
    return when (trend) {
        ReadingTimeTrend.Up -> MaterialTheme.colorScheme.primary
        ReadingTimeTrend.Down -> MaterialTheme.colorScheme.error
        ReadingTimeTrend.Flat, null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun EqualHeightGrid(
    columns: Int,
    spacing: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spacingPx = with(LocalDensity.current) { spacing.roundToPx() }
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val measurables = subcompose("measure", content)
        val rows = (measurables.size + columns - 1) / columns
        val cellWidth = ((constraints.maxWidth - spacingPx * (columns - 1)) / columns).coerceAtLeast(0)
        val loose = Constraints(
            minWidth = cellWidth,
            maxWidth = cellWidth,
            minHeight = 0,
            maxHeight = Constraints.Infinity,
        )
        val contentHeight = measurables.maxOfOrNull { it.measure(loose).height } ?: 0
        val exact = Constraints(
            minWidth = cellWidth,
            maxWidth = cellWidth,
            minHeight = contentHeight,
            maxHeight = contentHeight,
        )
        val placeables = subcompose("place", content).map { it.measure(exact) }
        val height = if (rows == 0) 0 else rows * contentHeight + spacingPx * (rows - 1)
        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val column = index % columns
                val row = index / columns
                placeable.place(
                    x = column * (cellWidth + spacingPx),
                    y = row * (contentHeight + spacingPx),
                )
            }
        }
    }
}
