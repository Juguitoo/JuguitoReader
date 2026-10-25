package com.juguito.juguitoreader.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

private val ChartPlotHeight = 140.dp

@Composable
internal fun StatsMinutesChart(
    period: StatsPeriod,
    buckets: List<ReadingStatsBucket>,
    totalMinutes: Int,
) {
    val summed = if (buckets.isNotEmpty()) buckets.sumOf { it.minutes } else totalMinutes
    val hasReading = buckets.any { it.minutes > 0 }
    val peakIndex = peakBucketIndex(buckets)
    var selectedIndex by remember(buckets) { mutableIntStateOf(peakIndex ?: -1) }
    val selected = buckets.getOrNull(selectedIndex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = statsCardBorder(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.stats_chart_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = LoraFontFamily,
                    )
                    Text(
                        text = paceLabel(period, buckets, summed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.stats_chart_total, formatReadingMinutes(summed)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            if (selected != null && hasReading) {
                val locale = LocalLocale.current.platformLocale
                val isPeak = selectedIndex == peakIndex
                Text(
                    text = stringResource(
                        R.string.stats_bar_value,
                        bucketReadout(period, selected.start, locale),
                        formatReadingMinutes(selected.minutes),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPeak) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            when {
                buckets.isEmpty() && summed > 0 -> Text(
                    text = stringResource(R.string.stats_chart_no_breakdown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
                !hasReading -> Text(
                    text = stringResource(R.string.stats_chart_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
                else -> MinutesBars(
                    period = period,
                    buckets = buckets,
                    selectedIndex = selectedIndex,
                    peakIndex = peakIndex,
                    onSelect = { selectedIndex = it },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun paceLabel(
    period: StatsPeriod,
    buckets: List<ReadingStatsBucket>,
    totalMinutes: Int,
): String {
    if (buckets.isEmpty()) return stringResource(R.string.stats_all)
    val pace = totalMinutes / buckets.size
    val formatted = formatReadingMinutes(pace)
    return if (period == StatsPeriod.YEAR) {
        stringResource(R.string.stats_pace_month, formatted)
    } else {
        stringResource(R.string.stats_pace_day, formatted)
    }
}

@Composable
private fun MinutesBars(
    period: StatsPeriod,
    buckets: List<ReadingStatsBucket>,
    selectedIndex: Int,
    peakIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val axisMax = chartAxisMax(buckets.maxOf { it.minutes })
    val locale = LocalLocale.current.platformLocale
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        ChartScale(
            maxMinutes = axisMax,
            modifier = Modifier
                .height(ChartPlotHeight)
                .padding(end = 8.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            ChartGuides(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(ChartPlotHeight),
            )
            if (period == StatsPeriod.WEEK) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    buckets.forEachIndexed { index, bucket ->
                        ReadingBar(
                            period = period,
                            bucket = bucket,
                            fraction = bucket.minutes / axisMax.toFloat(),
                            locale = locale,
                            isSelected = index == selectedIndex,
                            isPeak = index == peakIndex,
                            onClick = { onSelect(index) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    buckets.forEachIndexed { index, bucket ->
                        ReadingBar(
                            period = period,
                            bucket = bucket,
                            fraction = bucket.minutes / axisMax.toFloat(),
                            locale = locale,
                            isSelected = index == selectedIndex,
                            isPeak = index == peakIndex,
                            onClick = { onSelect(index) },
                            modifier = Modifier.width(if (period == StatsPeriod.MONTH) 32.dp else 40.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartScale(maxMinutes: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.stats_chart_axis, formatReadingMinutes(maxMinutes))
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End,
    ) {
        ScaleLabel(maxMinutes)
        ScaleLabel(maxMinutes / 2)
        ScaleLabel(0)
    }
}

@Composable
private fun ScaleLabel(minutes: Int) {
    Text(
        text = formatReadingMinutes(minutes),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun ChartGuides(modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(line),
            )
        }
    }
}

@Composable
private fun ReadingBar(
    period: StatsPeriod,
    bucket: ReadingStatsBucket,
    fraction: Float,
    locale: Locale,
    isSelected: Boolean,
    isPeak: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = bucketLabel(period, bucket.start, locale)
    val duration = formatReadingMinutes(bucket.minutes)
    val description = stringResource(
        R.string.stats_bar_description,
        bucketReadout(period, bucket.start, locale),
        duration,
    )
    val barColor = barColor(minutes = bucket.minutes, isPeak = isPeak, isSelected = isSelected)
    val labelColor = when {
        isPeak -> MaterialTheme.colorScheme.secondary
        isSelected -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val barHeight = with(LocalDensity.current) {
        val plotPx = ChartPlotHeight.toPx()
        (plotPx * fraction.coerceIn(0f, 1f))
            .coerceAtLeast(3.dp.toPx())
            .coerceAtMost(plotPx)
            .toDp()
    }
    Column(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                selected = isSelected
                contentDescription = description
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartPlotHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (bucket.minutes > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barColor),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isPeak || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.padding(top = 6.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun barColor(minutes: Int, isPeak: Boolean, isSelected: Boolean): Color {
    return when {
        minutes == 0 -> MaterialTheme.colorScheme.surfaceVariant
        isPeak -> MaterialTheme.colorScheme.secondary
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
    }
}
