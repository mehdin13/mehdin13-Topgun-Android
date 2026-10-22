// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** TODO(port): real implementation (see PlatformUi). */
internal object PaywallUi {
    @Composable fun Show(modifier: Modifier, onDismiss: () -> Unit, onPurchased: (days: Int) -> Unit) { Placeholder.Empty(modifier) }
}
