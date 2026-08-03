package com.juguito.juguitoreader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    secondary = CoralAccent,
    onSecondary = DeepNavy,
    tertiary = Pink80,
    background = DeepNavy,
    surface = DeepNavy,
    onBackground = PaperBackground,
    onSurface = PaperBackground,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = PaperBackground,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = CoralAccent,
    onSecondary = PaperBackground,
    secondaryContainer = CoralAccent.copy(alpha = 0.1f),
    background = PaperBackground,
    surface = PaperBackground,
    onBackground = DeepNavy,
    onSurface = DeepNavy,
    surfaceVariant = SurfaceVariant,
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

@Composable
fun JuguitoReaderTheme(
    appTheme: AppTheme = AppTheme.JUGUITO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.NEON -> NeonColorScheme
        AppTheme.JUGUITO -> LightColorScheme 
        AppTheme.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val statusBarColor = when (appTheme) {
            AppTheme.NEON -> NeonBackground
            AppTheme.JUGUITO -> TealStatus
            AppTheme.SYSTEM -> if (darkTheme) Color.Black else TealStatus
        }
        window.statusBarColor = statusBarColor.toArgb()

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
