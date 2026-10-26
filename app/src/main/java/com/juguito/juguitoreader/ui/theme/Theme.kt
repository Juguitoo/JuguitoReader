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
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    primaryFixed = DarkPrimaryFixed,
    primaryFixedDim = DarkPrimaryFixedDim,
    onPrimaryFixed = DarkOnPrimaryFixed,
    onPrimaryFixedVariant = DarkOnPrimaryFixedVariant,
    secondaryFixed = DarkSecondaryFixed,
    secondaryFixedDim = DarkSecondaryFixedDim,
    onSecondaryFixed = DarkOnSecondaryFixed,
    onSecondaryFixedVariant = DarkOnSecondaryFixedVariant,
    tertiaryFixed = DarkTertiaryFixed,
    tertiaryFixedDim = DarkTertiaryFixedDim,
    onTertiaryFixed = DarkOnTertiaryFixed,
    onTertiaryFixedVariant = DarkOnTertiaryFixedVariant,
)

val HighContrastDarkColorScheme = darkColorScheme(
    primary = NeonPrimary,
    onPrimary = NeonOnPrimary,
    primaryContainer = NeonPrimaryContainer,
    onPrimaryContainer = NeonOnPrimaryContainer,
    secondary = NeonSecondary,
    onSecondary = NeonOnSecondary,
    secondaryContainer = NeonSecondaryContainer,
    onSecondaryContainer = NeonOnSecondaryContainer,
    tertiary = NeonTertiary,
    onTertiary = NeonOnTertiary,
    tertiaryContainer = NeonTertiaryContainer,
    onTertiaryContainer = NeonOnTertiaryContainer,
    background = NeonBackground,
    onBackground = NeonText,
    surface = NeonSurface,
    onSurface = NeonText,
    surfaceVariant = NeonSurfaceVariant,
    onSurfaceVariant = NeonOnSurfaceVariant,
    surfaceTint = NeonPrimary,
    inverseSurface = NeonInverseSurface,
    inverseOnSurface = NeonInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = NeonOutline,
    outlineVariant = NeonOutlineVariant,
    surfaceBright = NeonSurfaceBright,
    surfaceDim = NeonBackground,
    surfaceContainerLowest = NeonSurfaceContainerLowest,
    surfaceContainerLow = NeonSurfaceContainerLow,
    surfaceContainer = NeonSurfaceContainer,
    surfaceContainerHigh = NeonSurfaceContainerHigh,
    surfaceContainerHighest = NeonSurfaceContainerHighest,
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

@Composable
fun chromeIconColor(): Color = when (MaterialTheme.colorScheme.background) {
    PastelBackground -> PastelIcon
    DarkBackground -> DarkIcon
    NeonBackground -> NeonIcon
    else -> JuguitoIcon
}

@Composable
fun chromeNavInactiveColor(): Color = when (MaterialTheme.colorScheme.background) {
    PastelBackground -> PastelNavInactive
    DarkBackground -> DarkNavInactive
    NeonBackground -> NeonNavInactive
    else -> JuguitoNavInactive
}
