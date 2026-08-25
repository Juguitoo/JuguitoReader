package com.juguito.juguitoreader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JuguitoColorScheme = lightColorScheme(
    primary = JuguitoPrimary,
    onPrimary = Color.White,
    primaryContainer = JuguitoPrimaryContainer,
    onPrimaryContainer = JuguitoOnPrimaryContainer,
    secondary = JuguitoSecondary,
    onSecondary = JuguitoOnBackground,
    background = JuguitoBackground,
    surface = JuguitoBackground,
    onBackground = JuguitoOnBackground,
    onSurface = JuguitoOnBackground,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = SoftGray,
    error = ErrorRed
)

private val PastelColorScheme = lightColorScheme(
    primary = PastelPink,
    onPrimary = Color.White,
    primaryContainer = PastelPinkContainer,
    onPrimaryContainer = PastelText,
    secondary = PastelPurple,
    onSecondary = Color.White,
    secondaryContainer = PastelPurpleContainer,
    onSecondaryContainer = PastelText,
    tertiary = PastelCoral,
    background = PastelBackground,
    surface = PastelBackground,
    onBackground = PastelText,
    onSurface = PastelText,
    surfaceVariant = PastelPurpleContainer.copy(alpha = 0.5f),
    onSurfaceVariant = SoftGray,
    error = ErrorRed
)

private val ClassicDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFF4A1000),
    onBackground = DarkText,
    onSurface = DarkText,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer
)

val HighContrastDarkColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    tertiary = NeonTertiary,
    background = NeonBackground,
    surface = NeonBackground,
    onPrimary = Color(0xFF003258),
    onSecondary = Color(0xFF381563),
    onTertiary = Color(0xFF410015),
    onBackground = NeonText,
    onSurface = NeonText,
    primaryContainer = NeonPrimaryContainer,
    onPrimaryContainer = NeonOnPrimaryContainer,
    secondaryContainer = NeonSecondaryContainer,
    onSecondaryContainer = NeonOnSecondaryContainer,
    tertiaryContainer = NeonTertiaryContainer,
    onTertiaryContainer = NeonOnTertiaryContainer
)

@Composable
fun JuguitoReaderTheme(
    appTheme: AppTheme = AppTheme.JUGUITO,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.JUGUITO -> JuguitoColorScheme
        AppTheme.PASTEL -> PastelColorScheme
        AppTheme.HIGH_CONTRAST -> HighContrastDarkColorScheme
        AppTheme.DARK -> ClassicDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            val isLight = colorScheme == JuguitoColorScheme || colorScheme == PastelColorScheme

            insetsController.isAppearanceLightStatusBars = isLight
            insetsController.isAppearanceLightNavigationBars = isLight

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
