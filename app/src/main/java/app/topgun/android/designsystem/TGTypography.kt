package app.topgun.android.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Text styles used by the iOS screens. Sizes are in sp so the user's font scale applies.
 * iOS: `SailecFont(.regular|.medium|.bold, size:)`, `.font(.system(size:, weight: .black, design: .rounded))`
 * for section titles (no rounded face on Android: system sans at Black weight stands in), and Futura Bold for chips.
 */
object TGText {
    fun sailec(weight: FontWeight, size: TextUnit) = TextStyle(fontFamily = TGFonts.sailec, fontWeight = weight, fontSize = size)
    fun sailecRegular(size: Int) = sailec(FontWeight.Normal, size.sp)
    fun sailecMedium(size: Int) = sailec(FontWeight.Medium, size.sp)
    fun sailecBold(size: Int) = sailec(FontWeight.Bold, size.sp)
    fun futuraBold(size: Int) = TextStyle(fontFamily = TGFonts.futura, fontWeight = FontWeight.Bold, fontSize = size.sp)
    fun rounded(size: Int, weight: FontWeight = FontWeight.Black) =
        TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = weight, fontSize = size.sp)

    /** "Welcome on board!" / `.title` bold. */
    val title = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    /** 20 black rounded: "Category", "Aircraft List". */
    val sectionTitle = rounded(20)
    /** 30 black rounded: "Top Gun" under the logo. */
    val logoTitle = rounded(30)
    val body = sailecRegular(14)
    val caption = sailecRegular(10)
}

internal fun materialTypography(): Typography {
    val base = Typography()
    return base.copy(
        titleLarge = TGText.title,
        titleMedium = TGText.sailecBold(16),
        bodyLarge = TGText.sailecRegular(16),
        bodyMedium = TGText.sailecRegular(14),
        bodySmall = TGText.sailecRegular(12),
        labelLarge = TGText.sailecMedium(14),
        labelMedium = TGText.sailecMedium(12),
        labelSmall = TGText.sailecRegular(10),
    )
}

/** System-font styles used by the Home feature cards and filter sheet (iOS `.headline`, `.body`, `.caption` default fonts). */
object TGHomeText {
    /** `.headline` bold: feature card title, filter section header. */
    val headline = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    /** Default `Text` in a List row. */
    val body = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 17.sp)
    /** `.caption`: feature card subtitle, "From: 100 Milion". */
    val caption = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    /** Search field text (`TextField` default). */
    val field = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp)
    /** EmptyStateView title 18 semibold; Retry 16 semibold. */
    val emptyTitle = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
}
