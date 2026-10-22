// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** TODO(port): real implementation (see PlatformUi). */
internal object VideoUi {
    @Composable fun Player(url: String, modifier: Modifier) { Placeholder.Empty(modifier) }
    @Composable fun YouTube(url: String, modifier: Modifier) { Placeholder.Empty(modifier) }
}
