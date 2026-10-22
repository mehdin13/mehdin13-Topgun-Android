// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** TODO(port): real implementation (see PlatformUi). */
internal object ModelUi {
    @Composable fun Viewer(modelUrl: String, modifier: Modifier) { Placeholder.Empty(modifier) }
    @Composable fun Ar(modelUrl: String, modifier: Modifier, onClose: () -> Unit) { Placeholder.Empty(modifier) }
}
