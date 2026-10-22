package app.topgun.android

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.topgun.android.platform.GoogleAccount
import app.topgun.android.platform.KeyValueStore
import app.topgun.android.platform.ModelCacheInfo
import app.topgun.android.platform.PlatformServices
import app.topgun.android.platform.ProStatus
import app.topgun.android.platform.ui.MapPin
import app.topgun.android.platform.ui.PlatformUi

/** Stand-ins used only by tools/compose-check (and as examples for previews/tests). */
class FakeKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, Any?>()
    override fun getString(key: String): String? = map[key] as? String
    override fun putString(key: String, value: String?) { map[key] = value }
    override fun getBoolean(key: String, default: Boolean): Boolean = map[key] as? Boolean ?: default
    override fun putBoolean(key: String, value: Boolean) { map[key] = value }
}

class FakePlatformServices : PlatformServices {
    override suspend fun signInWithGoogle(): GoogleAccount? = GoogleAccount("pilot@example.com", "Pilot", "One", null)
    override suspend fun signOutGoogle() {}
    override fun openUrl(url: String) {}
    override fun share(text: String) {}
    override fun openRateApp() {}
    override fun sendEmail(to: String, subject: String) {}
    override fun vibrate() {}
    override suspend fun currentProStatus(): ProStatus = ProStatus.Free
    override suspend fun restorePurchases(): ProStatus = ProStatus.Free
    override fun modelCacheInfo(): ModelCacheInfo = ModelCacheInfo(0, 0L)
    override fun clearModelCache() {}
    override fun setNotificationsEnabled(enabled: Boolean) {}
}

object FakePlatformUi : PlatformUi {
    @Composable override fun VideoPlayer(url: String, modifier: Modifier) {}
    @Composable override fun Model3dViewer(modelUrl: String, modifier: Modifier) {}
    @Composable override fun ArViewer(modelUrl: String, modifier: Modifier, onClose: () -> Unit) {}
    @Composable override fun WorldMap(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {}
    @Composable override fun Globe(pins: List<MapPin>, modifier: Modifier, onPinClick: (MapPin) -> Unit) {}
    @Composable override fun PdfViewer(url: String, modifier: Modifier) {}
    @Composable override fun Lottie(rawName: String, modifier: Modifier, loop: Boolean, onFinished: () -> Unit) {}
    @Composable override fun Paywall(modifier: Modifier, onDismiss: () -> Unit, onPurchased: (days: Int) -> Unit) {}
    @Composable override fun YouTubePlayer(url: String, modifier: Modifier) {}
}
