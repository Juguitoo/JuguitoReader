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
    onPrimary = JuguitoOnPrimary,
    primaryContainer = JuguitoPrimaryContainer,
    onPrimaryContainer = JuguitoOnPrimaryContainer,
    inversePrimary = JuguitoPrimary,
    secondary = JuguitoSecondary,
    onSecondary = JuguitoOnBackground,
    secondaryContainer = JuguitoSecondaryContainer,
    onSecondaryContainer = JuguitoOnSecondaryContainer,
    tertiary = JuguitoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = JuguitoTertiaryContainer,
    onTertiaryContainer = JuguitoOnTertiaryContainer,
    background = JuguitoBackground,
    onBackground = JuguitoOnBackground,
    surface = JuguitoBackground,
    onSurface = JuguitoOnBackground,
    surfaceVariant = JuguitoSurfaceVariant,
    onSurfaceVariant = JuguitoOnSurfaceVariant,
    surfaceTint = JuguitoPrimary,
    inverseSurface = JuguitoInverseSurface,
    inverseOnSurface = JuguitoBackground,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = JuguitoOutline,
    outlineVariant = JuguitoOutlineVariant,
    surfaceBright = JuguitoSurfaceContainerLowest,
    surfaceDim = JuguitoSurfaceDim,
    surfaceContainerLowest = JuguitoSurfaceContainerLowest,
    surfaceContainerLow = JuguitoSurfaceContainerLow,
    surfaceContainer = JuguitoSurfaceContainer,
    surfaceContainerHigh = JuguitoSurfaceContainerHigh,
    surfaceContainerHighest = JuguitoSurfaceContainerHighest,
    primaryFixed = JuguitoPrimaryContainer,
    primaryFixedDim = JuguitoPrimary,
    onPrimaryFixed = JuguitoOnPrimary,
    onPrimaryFixedVariant = JuguitoOnPrimaryContainer,
    secondaryFixed = JuguitoSecondaryContainer,
    secondaryFixedDim = JuguitoSecondary,
    onSecondaryFixed = JuguitoOnSecondaryContainer,
    onSecondaryFixedVariant = JuguitoOnPrimaryContainer,
    tertiaryFixed = JuguitoTertiaryContainer,
    tertiaryFixedDim = JuguitoTertiaryDim,
    onTertiaryFixed = JuguitoOnTertiaryContainer,
    onTertiaryFixedVariant = JuguitoOnTertiaryContainer,
)

private val PastelColorScheme = lightColorScheme(
    primary = PastelPink,
    onPrimary = PastelOnPrimary,
    primaryContainer = PastelPinkContainer,
    onPrimaryContainer = PastelText,
    inversePrimary = PastelPink,
    secondary = PastelPurple,
    onSecondary = PastelOnSecondary,
    secondaryContainer = PastelPurpleContainer,
    onSecondaryContainer = PastelText,
    tertiary = PastelCoral,
    onTertiary = PastelOnTertiary,
    tertiaryContainer = PastelTertiaryContainer,
    onTertiaryContainer = PastelOnTertiaryContainer,
    background = PastelBackground,
    onBackground = PastelText,
    surface = PastelBackground,
    onSurface = PastelText,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = PastelOnSurfaceVariant,
    surfaceTint = PastelPink,
    inverseSurface = PastelInverseSurface,
    inverseOnSurface = PastelBackground,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = PastelOutline,
    outlineVariant = PastelOutlineVariant,
    surfaceBright = Color.White,
    surfaceDim = PastelSurfaceDim,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PastelSurfaceContainerLow,
    surfaceContainer = PastelSurfaceContainer,
    surfaceContainerHigh = PastelSurfaceContainerHigh,
    surfaceContainerHighest = PastelSurfaceContainerHighest,
    primaryFixed = PastelPinkContainer,
    primaryFixedDim = PastelPink,
    onPrimaryFixed = PastelOnPrimary,
    onPrimaryFixedVariant = PastelText,
    secondaryFixed = PastelPurpleContainer,
    secondaryFixedDim = PastelPurple,
    onSecondaryFixed = PastelText,
    onSecondaryFixedVariant = PastelOnSecondary,
    tertiaryFixed = PastelTertiaryContainer,
    tertiaryFixedDim = PastelCoral,
    onTertiaryFixed = PastelOnTertiaryContainer,
    onTertiaryFixedVariant = PastelOnTertiary,
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
