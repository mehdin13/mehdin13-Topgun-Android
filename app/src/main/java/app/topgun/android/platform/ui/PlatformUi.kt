package app.topgun.android.platform.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** A pin on the map (country of operation, country capital or factory country). */
data class MapPin(val id: Int, val title: String, val subtitle: String?, val latitude: Double, val longitude: Double, val flagUrl: String? = null)

/**
 * UI that needs an Android-only library. Feature code calls these through [LocalPlatformUi]; the Android
 * implementations are in `// ANDROID-ONLY` files under `platform/ui/`.
 */
interface PlatformUi {
    /** In-app video (ExoPlayer / Media3) for a gallery video URL. */
    @Composable fun VideoPlayer(url: String, modifier: Modifier)

    /** Interactive 3D model viewer (SceneView). [modelUrl] is a downloadable glb/gltf; see docs/BACKEND_GAPS.md (backend ships usdz). */
    @Composable fun Model3dViewer(modelUrl: String, modifier: Modifier)

    /** Opens the model in AR (ARCore). */
    @Composable fun ArViewer(modelUrl: String, modifier: Modifier, onClose: () -> Unit)

    /** World map with pins (Google Maps). */
    @Composable fun WorldMap(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit)

    /** Spinning globe with pins (home of the country picker). */
    @Composable fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit)

    /** PDF viewer (android.graphics.pdf.PdfRenderer). */
    @Composable fun PdfViewer(url: String, modifier: Modifier)

    /** Lottie animation from `res/raw` by name (e.g. "splashlogo"). */
    @Composable fun Lottie(rawName: String, modifier: Modifier, loop: Boolean, onFinished: () -> Unit)

    /** RevenueCat paywall. [onPurchased] gets the number of days the purchase covers. */
    @Composable fun Paywall(modifier: Modifier, onDismiss: () -> Unit, onPurchased: (days: Int) -> Unit)

    /** YouTube player for `youtube_video` links. */
    @Composable fun YouTubePlayer(url: String, modifier: Modifier)
}

val LocalPlatformUi = staticCompositionLocalOf<PlatformUi> { error("PlatformUi not provided") }
