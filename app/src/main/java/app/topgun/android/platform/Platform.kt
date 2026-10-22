package app.topgun.android.platform

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
    fun setNotificationsEnabled(enabled: Boolean)
}

data class ModelCacheInfo(val fileCount: Int, val bytes: Long)
