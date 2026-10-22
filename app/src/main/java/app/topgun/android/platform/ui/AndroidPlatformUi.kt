// ANDROID-ONLY
package app.topgun.android.platform.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Aggregates the Android implementations; each lives in its own file (VideoUi, ModelUi, MapUi, PdfUi, LottieUi, PaywallUi). */
object AndroidPlatformUi : PlatformUi {
    @Composable override fun VideoPlayer(url: String, modifier: Modifier) = VideoUi.Player(url, modifier)
    @Composable override fun Model3dViewer(modelUrl: String, modifier: Modifier) = ModelUi.Viewer(modelUrl, modifier)
    @Composable override fun ArViewer(modelUrl: String, modifier: Modifier, onClose: () -> Unit) = ModelUi.Ar(modelUrl, modifier, onClose)
    @Composable override fun WorldMap(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) = MapUi.World(pins, modifier, onPinClick)
    @Composable override fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) = MapUi.Globe(pins, modifier, onPinClick)
    @Composable override fun PdfViewer(url: String, modifier: Modifier) = PdfUi.Viewer(url, modifier)
    @Composable override fun Lottie(rawName: String, modifier: Modifier, loop: Boolean, onFinished: () -> Unit) = LottieUi.Play(rawName, modifier, loop, onFinished)
    @Composable override fun Paywall(modifier: Modifier, onDismiss: () -> Unit, onPurchased: (days: Int) -> Unit) = PaywallUi.Show(modifier, onDismiss, onPurchased)
    @Composable override fun YouTubePlayer(url: String, modifier: Modifier) = VideoUi.YouTube(url, modifier)
}

internal object Placeholder {
    @Composable fun Empty(modifier: Modifier) { Box(modifier) }
}
