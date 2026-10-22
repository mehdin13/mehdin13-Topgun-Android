// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** TODO(port): real implementation (see PlatformUi). */
internal object LottieUi {
    @Composable fun Play(rawName: String, modifier: Modifier, loop: Boolean, onFinished: () -> Unit) { Placeholder.Empty(modifier) }
}
