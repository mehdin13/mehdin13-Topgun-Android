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
