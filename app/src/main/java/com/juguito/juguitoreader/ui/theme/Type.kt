package com.juguito.juguitoreader.ui.theme

import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as DownloadableFont
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.juguito.juguitoreader.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val JakartaFontName = GoogleFont("Plus Jakarta Sans")

// Bundled variable font from fonts.google.com. Play Services still serves an older Lora.
private fun loraFace(weight: FontWeight) = Font(
    resId = R.font.lora,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val LoraFontFamily = FontFamily(
    loraFace(FontWeight.Normal),
    loraFace(FontWeight.Medium),
    loraFace(FontWeight.Bold),
)

val JakartaFontFamily = FontFamily(
    DownloadableFont(googleFont = JakartaFontName, fontProvider = provider, weight = FontWeight.Normal),
    DownloadableFont(
        googleFont = JakartaFontName,
        fontProvider = provider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
    ),
    DownloadableFont(googleFont = JakartaFontName, fontProvider = provider, weight = FontWeight.Medium),
    DownloadableFont(googleFont = JakartaFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    DownloadableFont(googleFont = JakartaFontName, fontProvider = provider, weight = FontWeight.Bold),
)

private val baseTypography = MaterialTypography()

private fun TextStyle.asTitle(weight: FontWeight? = null) = copy(
    fontFamily = LoraFontFamily,
    fontWeight = weight ?: fontWeight,
)

private fun TextStyle.asBody(weight: FontWeight? = null) = copy(
    fontFamily = JakartaFontFamily,
    fontWeight = weight ?: fontWeight,
)

val Typography = MaterialTypography(
    displayLarge = baseTypography.displayLarge.asTitle(FontWeight.Bold).copy(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = baseTypography.displayMedium.asTitle(),
    displaySmall = baseTypography.displaySmall.asTitle(),
    headlineLarge = baseTypography.headlineLarge.asTitle(),
    headlineMedium = baseTypography.headlineMedium.asTitle(FontWeight.Bold).copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = baseTypography.headlineSmall.asTitle(),
    titleLarge = baseTypography.titleLarge.asTitle(FontWeight.Bold).copy(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = baseTypography.titleMedium.asTitle(),
    titleSmall = baseTypography.titleSmall.asTitle(),
    bodyLarge = baseTypography.bodyLarge.asBody(FontWeight.Normal).copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = baseTypography.bodyMedium.asBody(FontWeight.Normal).copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = baseTypography.bodySmall.asBody(),
    labelLarge = baseTypography.labelLarge.asBody(),
    labelMedium = baseTypography.labelMedium.asBody(FontWeight.Medium).copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = baseTypography.labelSmall.asBody(),
)
