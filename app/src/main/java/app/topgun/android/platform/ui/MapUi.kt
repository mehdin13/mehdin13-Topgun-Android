// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** TODO(port): real implementation (see PlatformUi). */
internal object MapUi {
    @Composable fun World(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) { Placeholder.Empty(modifier) }
    @Composable fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) { Placeholder.Empty(modifier) }
}
