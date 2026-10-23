// ANDROID-ONLY
package app.topgun.android.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Android implementation of [PlatformServices]. Google sign-in lives in [GoogleSignInHelper], purchases in
 * [BillingHelper] and the 3D model cache in [ModelCache], so each can be built and reviewed on its own.
 */
class AndroidPlatform(private val context: Context) : PlatformServices {
    private val google = GoogleSignInHelper(context)
    private val billing = BillingHelper(context)
    private val models = ModelCache(context)

    override suspend fun signInWithGoogle(): GoogleAccount? = google.signIn()
    override suspend fun signOutGoogle() = google.signOut()

    override fun openUrl(url: String) = start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    override fun share(text: String) = start(
        Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null),
    )

    override fun openRateApp() = openUrl("https://play.google.com/store/apps/details?id=${context.packageName}")

    override fun sendEmail(to: String, subject: String) = start(
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$to")).putExtra(Intent.EXTRA_SUBJECT, subject),
    )

    @Suppress("DEPRECATION")
    override fun vibrate() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override suspend fun currentProStatus(): ProStatus = billing.currentStatus()
    override suspend fun restorePurchases(): ProStatus = billing.restore()

    override fun modelCacheInfo(): ModelCacheInfo = models.info()
    override fun clearModelCache() = models.clear()
    override fun setNotificationsEnabled(enabled: Boolean) { /* TODO(port): FCM topic subscribe/unsubscribe */ }

    /** One activity-alias per launcher icon (see AndroidManifest.xml); exactly one is enabled. */
    private val iconAliases = listOf("IconDefault", "IconAlt1", "IconAlt2", "IconAlt3", "IconAlt4")

    private fun aliasComponent(index: Int) = ComponentName(context, "app.topgun.android.${iconAliases[index]}")

    override fun setAppIcon(index: Int) {
        if (index !in iconAliases.indices) return
        val pm = context.packageManager
        // Enable the chosen alias first so the app is never left without a launcher entry.
        pm.setComponentEnabledSetting(aliasComponent(index), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        iconAliases.indices.filter { it != index }.forEach {
            pm.setComponentEnabledSetting(aliasComponent(it), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }

    override fun currentAppIcon(): Int {
        val pm = context.packageManager
        val enabled = iconAliases.indices.firstOrNull {
            when (pm.getComponentEnabledSetting(aliasComponent(it))) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> it == 0 // manifest default: only the first alias is enabled
                else -> false
            }
        }
        return enabled ?: 0
    }

    private fun start(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
