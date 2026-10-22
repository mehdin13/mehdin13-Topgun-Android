package com.mehdin13.dreamland.designsystem

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

val LocalDLColors = compositionLocalOf<DLPalette> { DLLightPalette }
val LocalDLTypography = compositionLocalOf { DLTypographyTokens() }

/** Access to design tokens: `DL.colors.accentPrimary`, `DL.type.headline`. */
object DL {
    val colors: DLPalette
        @Composable @ReadOnlyComposable get() = LocalDLColors.current
    val type: DLTypographyTokens
        @Composable @ReadOnlyComposable get() = LocalDLTypography.current
}

/** True when the system asks for higher contrast (Android 14+ contrast setting). */
@Composable
private fun rememberHighContrast(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= 34) {
            val ui = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            (ui?.contrast ?: 0f) >= 0.5f
        } else {
            false
        }
    }
}

/**
 * Dreamland theme: the four token palettes (light, dark, and increased-contrast variants of each)
 * drive both the design-system components and a Material 3 color scheme for stock components.
 * Dynamic color (wallpaper) is deliberately not used: the brand palette is the design.
 */
@Composable
fun DreamlandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = rememberHighContrast(),
    content: @Composable () -> Unit,
) {
    val palette = when {
        darkTheme && highContrast -> DLDarkHighContrastPalette
        darkTheme -> DLDarkPalette
        highContrast -> DLLightHighContrastPalette
        else -> DLLightPalette
    }
    val type = remember { DLTypographyTokens() }
    CompositionLocalProvider(LocalDLColors provides palette, LocalDLTypography provides type) {
        MaterialTheme(
            colorScheme = palette.toMaterial(darkTheme),
            typography = type.toMaterial(),
            shapes = Shapes(
                extraSmall = DLRadius.xsShape,
                small = DLRadius.smShape,
                medium = DLRadius.mdShape,
                large = DLRadius.lgShape,
                extraLarge = DLRadius.xlShape,
            ),
            content = content,
        )
    }
}

private fun DLPalette.toMaterial(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accentPrimary,
        onPrimary = textOnAccent,
        primaryContainer = accentPrimarySoft,
        onPrimaryContainer = accentPrimaryText,
        secondary = accentSecondary,
        onSecondary = textOnAccent,
        secondaryContainer = accentSecondarySoft,
        onSecondaryContainer = accentSecondaryText,
        tertiary = accentFavorite,
        onTertiary = textOnAccent,
        tertiaryContainer = accentFavoriteSoft,
        onTertiaryContainer = accentFavoriteText,
        background = backgroundPrimary,
        onBackground = textPrimary,
        surface = surfaceCard,
        onSurface = textPrimary,
        surfaceVariant = backgroundSecondary,
        onSurfaceVariant = textSecondary,
        surfaceContainer = backgroundSecondary,
        surfaceContainerHigh = backgroundElevated,
        surfaceContainerHighest = backgroundElevated,
        outline = borderStrong,
        outlineVariant = borderSubtle,
        error = error,
        onError = backgroundPrimary,
        errorContainer = errorSoft,
        onErrorContainer = error,
        scrim = scrim,
    )
}
