package com.juguito.juguitoreader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    onPrimary = NeonBackground,
    primaryContainer = NeonPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = NeonPrimary,
    secondary = NeonSecondary,
    onSecondary = NeonBackground,
    tertiary = NeonAccent,
    background = NeonBackground,
    surface = NeonSurface,
    onBackground = NeonText,
    onSurface = NeonText,
    surfaceVariant = NeonSurface,
    onSurfaceVariant = NeonSecondary.copy(alpha = 0.7f),
    error = ErrorRed
)

// Esquema de sistema
private val SystemDarkColorScheme = darkColorScheme(
    primary = JuguitoPrimaryContainer,
    onPrimary = JuguitoOnPrimaryContainer,
    background = DeepNavy,
    surface = DeepNavy,
    onBackground = JuguitoBackground,
    onSurface = JuguitoBackground
)

@Composable
fun JuguitoReaderTheme(
    appTheme: AppTheme = AppTheme.JUGUITO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.JUGUITO -> JuguitoColorScheme
        AppTheme.PASTEL -> PastelColorScheme
        AppTheme.NEON -> NeonColorScheme
        AppTheme.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) SystemDarkColorScheme else JuguitoColorScheme
            }
        }
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
