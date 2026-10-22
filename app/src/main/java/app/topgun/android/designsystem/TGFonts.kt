// ANDROID-ONLY
package app.topgun.android.designsystem

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.topgun.android.R

/** Font families bundled from the iOS app (`Resource/Font`): Sailec for body text, Futura for headings, MonaKo for accents. */
object TGFonts {
    val sailec = FontFamily(
        Font(R.font.sailec_regular, FontWeight.Normal),
        Font(R.font.sailec_medium, FontWeight.Medium),
        Font(R.font.sailec_bold, FontWeight.Bold),
    )
    val futura = FontFamily(
        Font(R.font.futura_light, FontWeight.Light),
        Font(R.font.futura_book, FontWeight.Normal),
        Font(R.font.futura_medium, FontWeight.Medium),
        Font(R.font.futura_bold, FontWeight.Bold),
        Font(R.font.futura_heavy, FontWeight.ExtraBold),
        Font(R.font.futura_extrablack, FontWeight.Black),
    )
    val monako = FontFamily(Font(R.font.monako))
}
