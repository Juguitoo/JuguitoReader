package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeSectionHeader(
    modifier: Modifier = Modifier,
    iconImage: ImageVector? = null,
    title: String,
    badge: String? = null,
    badgeContentColor: Color = MaterialTheme.colorScheme.primary,
    badgeOutlined: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actionChevron: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (iconImage != null) {
                Icon(imageVector = iconImage, contentDescription = "")
            }

            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = homeSerif(size = 18.sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeContentColor.copy(alpha = if (badgeOutlined) 0.10f else 0.15f),
                    border = BorderStroke(1.dp, badgeContentColor)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = homeSans(size = 12.sp, fontWeight = FontWeight.Medium),
                        color = badgeContentColor
                    )
                }
            }
        }
        if (actionLabel != null && onAction != null) {
            SectionLink(
                label = actionLabel,
                onClick = onAction,
                color = actionColor,
                showChevron = actionChevron
            )
        }
    }
}

@Composable
internal fun SectionLink(
    label: String,
    onClick: () -> Unit,
    color: Color,
    showChevron: Boolean = false,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick, role = Role.Button)
            .padding(PaddingValues(horizontal = 8.dp, vertical = 8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = homeSans(size = 12.sp),
            color = color
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
        }
    }
}
