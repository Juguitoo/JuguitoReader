package com.juguito.juguitoreader.ui.stats.components

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.ui.common.toUiText
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.time.LocalDate
import kotlin.math.abs

private val selectablePeriods = listOf(StatsPeriod.WEEK, StatsPeriod.MONTH, StatsPeriod.YEAR)

@Composable
internal fun StatsPeriodSelector(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
) {
    val selectedIndex = selectablePeriods.indexOf(selected).coerceAtLeast(0)
    val reduceMotion = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
    val indicatorIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        },
        label = "periodIndicator",
    )
    val activeContent = MaterialTheme.colorScheme.onPrimary
    val inactiveContent = MaterialTheme.colorScheme.onSurface
    val shape = CircleShape

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .selectableGroup()
    ) {
        val inset = 4.dp
        val segmentWidth = (maxWidth - inset * 2) / selectablePeriods.size
        Box(
            modifier = Modifier
                .offset(x = inset + segmentWidth * indicatorIndex)
                .padding(vertical = inset)
                .width(segmentWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary, shape)
        )
        Row(Modifier.fillMaxSize()) {
            selectablePeriods.forEachIndexed { index, period ->
                val distance = abs(indicatorIndex - index).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = period == selected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onSelect(period) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = period.toUiText().asString(),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        color = lerp(activeContent, inactiveContent, distance),
                    )
                }
            }
        }
    }
}

@Composable
internal fun StatsPeriodPager(
    period: StatsPeriod,
    anchor: LocalDate,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.stats_previous_period),
            )
        }
        Text(
            text = periodTitle(period, anchor),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = LoraFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.stats_next_period),
            )
        }
    }
}
