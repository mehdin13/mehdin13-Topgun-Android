package app.topgun.android.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

val LocalTGPalette = compositionLocalOf { TGLightPalette }

/** Access to the app palette: `TG.colors.background`. Brand colors that never change are in [TGBrand]. */
object TG {
    val colors: TGPalette
        @Composable @ReadOnlyComposable get() = LocalTGPalette.current
}

/**
 * Top Gun theme. The iOS palette (light + dark asset colors) drives both the app's own components and a
 * Material 3 color scheme, so stock Material components match. Dynamic (wallpaper) color is not used.
 * [darkTheme] follows the in-app Appearance setting (System / Light / Dark).
 */
@Composable
fun TopGunTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) TGDarkPalette else TGLightPalette
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent, onPrimary = TGBrand.backdropBlack,
            secondary = TGBrand.primaryRed, tertiary = TGBrand.yellow,
            background = palette.background, onBackground = palette.textPrimary,
            surface = palette.card, onSurface = palette.textPrimary,
            surfaceVariant = palette.card, onSurfaceVariant = palette.textSecondary,
            outline = TGBrand.gray.copy(alpha = 0.4f), error = TGBrand.red,
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = TGBrand.formGray,
            secondary = TGBrand.primaryRed, tertiary = TGBrand.yellow,
            background = palette.background, onBackground = palette.textPrimary,
            surface = palette.card, onSurface = palette.textPrimary,
            surfaceVariant = palette.card, onSurfaceVariant = palette.textSecondary,
            outline = TGBrand.gray, error = TGBrand.red,
        )
    }
    CompositionLocalProvider(LocalTGPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = materialTypography(),
            shapes = Shapes(
                extraSmall = TGRadius.chip, small = TGRadius.button, medium = TGRadius.card,
                large = TGRadius.card, extraLarge = TGRadius.tabBar,
            ),
            content = content,
        )
    }
}
