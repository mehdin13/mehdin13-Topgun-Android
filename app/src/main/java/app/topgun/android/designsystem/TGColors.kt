package app.topgun.android.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Colors from the iOS app: the asset catalog (`ColorSet`, with light/dark) and `Extensions/Utils.swift`.
 * Names follow the iOS names so screens can be compared side by side.
 */
class TGPalette(
    /** iOS `primaryCol`: screen background. */
    val background: Color,
    /** iOS `secondaryCol`: cards and list rows. */
    val card: Color,
    /** iOS `text_primary_color`. */
    val textPrimary: Color,
    /** iOS `main_color`: links and primary actions. */
    val accent: Color,
    val isDark: Boolean,
) {
    val textSecondary: Color get() = textPrimary.copy(alpha = 0.7f)
}

val TGLightPalette = TGPalette(
    background = Color(0xFFF4F7FD),
    card = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1E3054),
    accent = Color(0xFF006AF6),
    isDark = false,
)

val TGDarkPalette = TGPalette(
    background = Color(0xFF0C1B3A),
    card = Color(0xFF162544),
    textPrimary = Color(0xFFF5CAC9),
    accent = Color(0xFF147EFF),
    isDark = true,
)

/** Fixed brand colors (`Color` extension in iOS `Utils.swift` and `ColorExtension.swift`), the same in light and dark. */
object TGBrand {
    val primaryRed = Color(0xFFFF385B)      // iOS primaryColor: ball of the tab bar, key accents
    val navy = Color(0xFF232F41)            // secondeyColor
    val yellow = Color(0xFFFDDA01)          // thirdColor
    val beige = Color(0xFFFAE6CF)           // forthColor
    val gray = Color(0xFFDCDCDC)            // grayColor
    val formGray = Color(0xFFEBEAEF)        // formColor
    val darkBack = Color(0xFF262530)        // DarkBackColor
    val backdropBlack = Color(0xFF272630)   // backgroudBlackColor
    val lightWhite = Color(0xFFD7DDE8)      // LightWhiteColor (tab bar in light mode)
    val tabIdle = Color(0xFF0C0C0C)         // exampleGrey
    val gold = Color(0xFFFEA842)            // asset "Gold"
    val red = Color(0xFFEB5757)             // red_color
    val blue = Color(0xFF006AF6)            // blue_color
    val green = Color(0xFF3EFFA2)           // asset "Green" / "LightGreen"
    val darkRed = Color(0xFF962121)         // asset "DarkRed"
    val orange = Color(0xFFFF9500)          // SwiftUI .orange (year tags)
    val placeholder = Color(0xFFBDBDBD)     // placeholder_color
}

/** Colors used only by the Favorites / Settings / Blog / Badges screens: SwiftUI system colors named in iOS SettingView and FavoriteView. */
object TGAccountColors {
    val systemGreen = Color(0xFF34C759)     // SwiftUI .green (notification icon)
    val systemPurple = Color(0xFFAF52DE)    // SwiftUI .purple (language, share, Instagram)
    val systemBrown = Color(0xFFA2845E)     // SwiftUI .brown (unit system icon)
    val white = Color(0xFFFFFFFF)           // FavoriteView heart button fill, SettingView avatar stroke
    val systemYellow = Color(0xFFFFCC00)    // SwiftUI .yellow (badges, rate, theme icons)
}

/** Colors used only by the Home screen and filter sheet: iOS HomeView.swift (category tiles, load-more gradient) and FeaturedSectionItem.swift. */
object TGHomeColors {
    val systemRed = Color(0xFFFF3B30)       // SwiftUI .red (Fighter tile)
    val systemBlue = Color(0xFF007AFF)      // SwiftUI .blue (Bomber tile)
    val systemGreen = Color(0xFF34C759)     // SwiftUI .green (Reconnaissance tile)
    val systemGray = Color(0xFF8E8E93)      // SwiftUI .gray (Passenger tile, search icons, unselected radio)
    val systemPurple = Color(0xFFAF52DE)    // SwiftUI .purple (Electronic Warfare tile, load-more gradient end)
    val lime = Color(0xFFB0F353)            // Color(hex: "B0F353") Transport tile
    val coral = Color(0xFFFD5D5D)           // "FD5D5D" Helicopters tile
    val ember = Color(0xFFF56B42)           // "F56B42" Tanker tile
    val slate = Color(0xFF8A8899)           // "8A8899" UAVs tile
    val sky = Color(0xFF47DCFE)             // "47DCFE" Trainer tile
    val crimson = Color(0xFFEC3254)         // "EC3254" AWACS tile
    val white = Color(0xFFFFFFFF)           // text/icons on gradients and the load-more button
    /** Category tile background opacity (`.opacity(0.1)`). */
    const val categoryTintAlpha = 0.1f
    /** Feature card icon circle `Color.white.opacity(0.2)` and subtitle `.opacity(0.8)`. */
    const val featureIconAlpha = 0.2f
    const val featureSubtitleAlpha = 0.8f

    /** `FeaturedSectionItem.defaultGradients()`: start/end colors, assigned to the cards in order. */
    val featureGradients: List<Pair<Color, Color>> = listOf(
        Color(0.98f, 0.53f, 0.28f) to Color(0.99f, 0.35f, 0.58f),
        Color(0.2f, 0.6f, 0.8f) to Color(0.3f, 0.8f, 0.9f),
        Color(0.7f, 0.4f, 0.9f) to Color(0.9f, 0.6f, 0.8f),
        Color(0.3f, 0.8f, 0.5f) to Color(0.6f, 0.9f, 0.7f),
        Color(0.2f, 0.3f, 0.6f) to Color(0.8f, 0.2f, 0.2f),
        Color(0.1f, 0.1f, 0.1f) to Color(0.3f, 0.3f, 0.4f),
        Color(0.0f, 0.2f, 0.5f) to Color(0.0f, 0.6f, 0.8f),
        Color(1.0f, 0.3f, 0.0f) to Color(1.0f, 0.8f, 0.0f),
    )
}

/**
 * Pure black / white used by the entry flow. iOS `Intro.swift` (`.black` capsule buttons, `.white` button text),
 * `SplashView.swift` (`Color.black` background) and `SignInView.swift` (white social buttons, black label).
 */
object TGEntryColors {
    val black = Color(0xFF000000)
    val white = Color(0xFFFFFFFF)
}

/** Colors for countries, manufacturers and the map. iOS: SwiftUI system `.purple` (cover placeholder gradient, "Air Force" button). */
object TGGeoColor {
    val purple = Color(0xFFAF52DE)
    /** Night-sky background behind the globe: iOS GlobeView `UIColor(red: 0.05, green: 0.05, blue: 0.1)`. */
    val globeSpace = Color(0xFF0D0D1A)
}

/**
 * Aircraft detail screen colors. iOS: `Extensions/Extensions.swift` gradients (asset color pairs, the second stop at 0.9 alpha),
 * `DetailsView+Components.swift` (`DetailsInfoView` text "828282" and stroke "DDDDDD").
 */
object TGDetailColors {
    /** `gridBackgroud`: asset "Background 1" 1.0 -> 0.5 (inactive tile). */
    val tileInactive = listOf(Color(0xFF1B2A41), Color(0xFF1B2A41).copy(alpha = 0.5f))
    /** `selectedGridBackgroud`: DarkBlue -> LightBlue. */
    val tileBlue = listOf(Color(0xFF0075FF), Color(0xFF90CAF9).copy(alpha = 0.9f))
    /** `gradiantOrange`: DarkOrange -> LightOrange. */
    val tileOrange = listOf(Color(0xFFF83600), Color(0xFFFE8C00).copy(alpha = 0.9f))
    /** `gradiantpink`: DarkPink -> LightPink. */
    val tilePink = listOf(Color(0xFFFB2524), Color(0xFFFD366A).copy(alpha = 0.9f))
    /** `gradiantCherry`: Cherry -> LightCherry. */
    val tileCherry = listOf(Color(0xFFEB3349), Color(0xFFF45C43).copy(alpha = 0.9f))
    /** `gradiantMojito`: Green2 -> LightGreen2. */
    val tileGreen = listOf(Color(0xFF21AB48), Color(0xFF6FC815).copy(alpha = 0.9f))

    /** `Color(hex: "828282")`: secondary text in info cells and the owner bio. */
    val infoText = Color(0xFF828282)
    /** `Color(hex: "DDDDDD")`: hairline around info cells. */
    val hairline = Color(0xFFDDDDDD)

    /** SwiftUI `.white` / `.black` on top of photos (header buttons, gallery, video and AR chrome). */
    val onPhoto = Color(0xFFFFFFFF)
    val photoBlack = Color(0xFF000000)
    /** SwiftUI `.green` pill (manufacturer) and its 15% tint. */
    val manufacturerGreen = Color(0xFF34C759)
}
