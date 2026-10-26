package com.juguito.juguitoreader.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import com.juguito.juguitoreader.utils.contrastingContent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
internal fun StatsHeatmap(
    month: YearMonth,
    weights: List<ReadingStatsBucket>,
    today: LocalDate,
    streak: Int,
    bestStreak: Int,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val minutesByDay = weights.groupBy { it.start }.mapValues { (_, days) -> days.sumOf { it.minutes } }
    val days = (1..month.lengthOfMonth()).map { dayOfMonth ->
        val date = month.atDay(dayOfMonth)
        date to (minutesByDay[date] ?: 0)
    }
    val peak = days.maxOf { it.second }
    val leadingBlanks = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
    var selected by remember(month, weights) { mutableStateOf<LocalDate?>(null) }
    val selectedMinutes = selected?.let { date -> days.firstOrNull { it.first == date }?.second }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = statsCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = monthTitle(month, locale),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = LoraFontFamily,
                    )
                }
                HeatLegend()
            }
            WeekdayHeader(locale)
            HeatGrid(
                leadingBlanks = leadingBlanks,
                days = days,
                peak = peak,
                today = today,
                selected = selected,
                locale = locale,
                onSelect = { selected = it },
            )
            val selectedDate = selected
            if (selectedDate != null && selectedMinutes != null) {
                val dateLabel = selectedDate.format(DateTimeFormatter.ofPattern("d MMM", locale))
                Text(
                    text = if (selectedMinutes > 0) {
                        stringResource(
                            R.string.stats_heatmap_day,
                            dateLabel,
                            formatReadingMinutes(selectedMinutes),
                        )
                    } else {
                        stringResource(R.string.stats_heatmap_none, dateLabel)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            StreakCallout(streak = streak, bestStreak = bestStreak)
        }
    }
}

@Composable
private fun HeatLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LegendDot(
            color = MaterialTheme.colorScheme.surfaceVariant,
            label = stringResource(R.string.stats_heatmap_inactive),
        )
        LegendDot(
            color = MaterialTheme.colorScheme.secondary,
            label = stringResource(R.string.stats_heatmap_read),
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeekdayHeader(locale: Locale) {
    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HeatGrid(
    leadingBlanks: Int,
    days: List<Pair<LocalDate, Int>>,
    peak: Int,
    today: LocalDate,
    selected: LocalDate?,
    locale: Locale,
    onSelect: (LocalDate) -> Unit,
) {
    val cells: List<Pair<LocalDate, Int>?> = buildList {
        repeat(leadingBlanks) { add(null) }
        addAll(days)
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { cell ->
                    if (cell == null) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        HeatDay(
                            date = cell.first,
                            minutes = cell.second,
                            peak = peak,
                            isToday = cell.first == today,
                            isSelected = cell.first == selected,
                            locale = locale,
                            onClick = { onSelect(cell.first) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
private fun HeatDay(
    date: LocalDate,
    minutes: Int,
    peak: Int,
    isToday: Boolean,
    isSelected: Boolean,
    locale: Locale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val intensity = heatIntensity(minutes, peak)
    val accent = MaterialTheme.colorScheme.secondary
    val background = if (intensity == 0f) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        lerp(MaterialTheme.colorScheme.surface, accent, intensity)
    }
    val contentColor = background.contrastingContent()
    val dateLabel = date.format(DateTimeFormatter.ofPattern("d MMM", locale))
    val description = if (minutes > 0) {
        stringResource(R.string.stats_heatmap_day, dateLabel, formatReadingMinutes(minutes))
    } else {
        stringResource(R.string.stats_heatmap_none, dateLabel)
    }
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .then(
                if (isToday || isSelected) {
                    Modifier.border(
                        width = if (isToday) 2.dp else 1.dp,
                        color = if (isToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        shape = shape,
                    )
                } else {
                    Modifier
                }
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                selected = isSelected
                contentDescription = description
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (intensity >= 0.72f || isToday) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StreakCallout(streak: Int, bestStreak: Int) {
    if (streak == 0 && bestStreak == 0) return
    val title: String
    val detail: String
    when {
        streak > 0 && streak >= bestStreak -> {
            title = stringResource(R.string.stats_streak_best)
            detail = stringResource(R.string.stats_streak_best_detail)
        }
        streak > 0 -> {
            title = pluralStringResource(R.plurals.stats_streak_active, streak, streak)
            detail = pluralStringResource(
                R.plurals.stats_streak_to_record,
                bestStreak - streak,
                bestStreak - streak,
            )
        }
        else -> {
            title = pluralStringResource(R.plurals.stats_streak_record_only, bestStreak, bestStreak)
            detail = stringResource(R.string.stats_streak_record_detail)
        }
    }
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
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
