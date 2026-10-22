package com.mehdin13.dreamland.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale from tokens.json. Sizes are in sp so Dynamic Type equivalents (system font scale) apply.
 *
 * Font families: the design uses Young Serif (display), SF Pro Rounded (interface) and New York (reading).
 * None ships with Android, so the system serif and sans fall back until the font files are added
 * (see docs/PROGRESS.md, open TODOs). Never register a font that is not in the repo.
 */
private val Display = FontFamily.Serif
private val Interface = FontFamily.SansSerif
private val Reading = FontFamily.Serif

class DLTypographyTokens(
    val displayLarge: TextStyle = TextStyle(fontFamily = Display, fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.5).sp),
    val displayMedium: TextStyle = TextStyle(fontFamily = Display, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.3).sp),
    val titleLarge: TextStyle = TextStyle(fontFamily = Interface, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.38.sp),
    val titleMedium: TextStyle = TextStyle(fontFamily = Interface, fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.45).sp),
    val headline: TextStyle = TextStyle(fontFamily = Interface, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.43).sp),
    val bodyLarge: TextStyle = TextStyle(fontFamily = Interface, fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.45).sp),
    val bodyRegular: TextStyle = TextStyle(fontFamily = Interface, fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.43).sp),
    val bodySmall: TextStyle = TextStyle(fontFamily = Interface, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.23).sp),
    val caption: TextStyle = TextStyle(fontFamily = Interface, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
    val buttonLabel: TextStyle = TextStyle(fontFamily = Interface, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.43).sp),
    val metadataLabel: TextStyle = TextStyle(fontFamily = Interface, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.08).sp),
    val storyBody: TextStyle = TextStyle(fontFamily = Reading, fontSize = 20.sp, lineHeight = 32.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
    val storyBodyLarge: TextStyle = TextStyle(fontFamily = Reading, fontSize = 26.sp, lineHeight = 40.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
    val storyDropcap: TextStyle = TextStyle(fontFamily = Reading, fontSize = 64.sp, lineHeight = 64.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
    val storyCaption: TextStyle = TextStyle(fontFamily = Reading, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
)

/** Material 3 type roles mapped onto the Dreamland scale, so stock M3 components stay on-brand. */
internal fun DLTypographyTokens.toMaterial(): Typography = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    headlineLarge = titleLarge,
    headlineMedium = titleMedium,
    titleLarge = titleMedium,
    titleMedium = headline,
    titleSmall = headline,
    bodyLarge = bodyRegular,
    bodyMedium = bodySmall,
    bodySmall = caption,
    labelLarge = buttonLabel,
    labelMedium = metadataLabel,
    labelSmall = caption,
)
