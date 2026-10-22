// ANDROID-ONLY
package app.topgun.android.platform

import android.content.Context
import android.content.Intent
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

    private fun start(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
