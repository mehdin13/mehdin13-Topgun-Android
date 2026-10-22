package app.topgun.android.designsystem

import androidx.compose.ui.text.font.FontFamily

/** Replaces the ANDROID-ONLY TGFonts (which needs R.font) in the JVM check. */
object TGFonts {
    val sailec: FontFamily = FontFamily.SansSerif
    val futura: FontFamily = FontFamily.SansSerif
    val monako: FontFamily = FontFamily.Monospace
}
