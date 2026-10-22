package app.topgun.android.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing and radius used across the iOS screens. Android dp = iOS pt. Do not hardcode other values in screens. */
object TGSpace {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    /** Space reserved at the bottom of scrolling screens so content clears the floating tab bar. */
    val tabBarClearance = 150.dp
}

object TGRadius {
    val chip = RoundedCornerShape(6.dp)
    val card = RoundedCornerShape(16.dp)
    val image = RoundedCornerShape(15.dp)
    val button = RoundedCornerShape(12.dp)
    val tabBar = RoundedCornerShape(30.dp)
}

object TGSize {
    /** Minimum touch target. */
    val tapMin = 48.dp
    val listThumbnail = 100.dp
    val gridCardHeight = 200.dp
}
