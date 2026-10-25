package app.topgun.android.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Everything the app needs from Android that is not UI. Features depend on these pure-Kotlin interfaces only;
 * the Android implementations live in files whose first line is `// ANDROID-ONLY` (they are skipped by
 * `tools/compose-check`, which type-checks the rest against a JVM Compose build with fakes for these interfaces).
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}

/** [idToken] is the Google ID token; the backend verifies it before trusting [email]. */
class GoogleAccount(val email: String, val firstName: String?, val lastName: String?, val photoUrl: String?, val idToken: String? = null)

sealed interface ProStatus {
    data object Free : ProStatus
    /** [days] is the length of the purchased period, sent to the backend after a purchase. */
    data class Pro(val days: Int) : ProStatus
}

/** Why the Google account step failed, so the sign-in screen can say what to fix (and tell it apart from a server problem). */
class GoogleSignInException(val kind: Kind, message: String?, cause: Throwable? = null) : Exception(message, cause) {
    enum class Kind {
        /** The build has no Google web client ID (GOOGLE_WEB_CLIENT_ID secret / topgun.googleWebClientId). */
        NotConfigured,
        /** Google found no usable account/credential: no Google account on the phone, or the Android OAuth client (package + SHA-1) does not match this APK. */
        NoAccount,
        /** Google Play services / Credential Manager provider is missing or too old. */
        ProviderMissing,
        Other,
    }
}

interface PlatformServices {
    /** Shows the Google account picker (Credential Manager). Returns null when the user cancels. Throws on failure. */
    suspend fun signInWithGoogle(): GoogleAccount?
    suspend fun signOutGoogle()

    fun openUrl(url: String)
    fun share(text: String)
    fun openRateApp()
    fun sendEmail(to: String, subject: String)
    fun vibrate()

    /** RevenueCat entitlement "Pro" (see iOS HomeView.handlePurchaseCompletion). */
    suspend fun currentProStatus(): ProStatus
    suspend fun restorePurchases(): ProStatus

    /** Size and count of downloaded 3D models, and clearing them (Settings > Clear Cache). */
    fun modelCacheInfo(): ModelCacheInfo
    fun clearModelCache()

    /** Per-model download state, explicit "download for offline" and removal (aircraft details > 3D Model). */
    val modelDownloads: ModelDownloads get() = NoModelDownloads
    fun setNotificationsEnabled(enabled: Boolean)

    /**
     * Launcher icon switching (iOS Settings > Change App icon). Index 0 is the default icon, 1..4 the alternates
     * (iOS AppIcon1..4). Android implements this with one activity-alias per icon; the launcher may take a few seconds to refresh.
     */
    fun setAppIcon(index: Int)
    fun currentAppIcon(): Int
}

data class ModelCacheInfo(val fileCount: Int, val bytes: Long)

/** State of one 3D model file (keyed by its resolved URL). */
sealed interface ModelDownloadState {
    data object NotDownloaded : ModelDownloadState

    /** [totalBytes] is null when the server sent no Content-Length: show an indeterminate indicator. */
    data class Downloading(val bytesRead: Long, val totalBytes: Long?) : ModelDownloadState {
        /** 0..1, or null when the size is unknown. */
        val fraction: Float? get() = totalBytes?.takeIf { it > 0 }?.let { (bytesRead.toDouble() / it).toFloat().coerceIn(0f, 1f) }
        val percent: Int? get() = fraction?.let { (it * 100).toInt() }
    }

    data class Downloaded(val bytes: Long) : ModelDownloadState
    data class Failed(val message: String) : ModelDownloadState
}

/**
 * Download manager for 3D models, shared by the details screen (buttons) and the viewer (progress). Downloads run in
 * an app-wide scope, so they continue when the screen is left. Files are kept until [remove] or Settings > Clear Cache.
 */
interface ModelDownloads {
    /** Live state for [url]; Downloaded when the file is already on the device. */
    fun state(url: String): StateFlow<ModelDownloadState>
    /** Starts an explicit download for offline use (no-op while one is running or when already downloaded). */
    fun download(url: String)
    fun cancel(url: String)
    /** Deletes the downloaded file. */
    fun remove(url: String)
}

/** Used by previews, tests and platforms without a model cache. */
object NoModelDownloads : ModelDownloads {
    private val idle = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded).asStateFlow()
    override fun state(url: String): StateFlow<ModelDownloadState> = idle
    override fun download(url: String) {}
    override fun cancel(url: String) {}
    override fun remove(url: String) {}
}

/** "12.3 MB" style size for download labels. */
fun formatByteCount(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
    else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
}
