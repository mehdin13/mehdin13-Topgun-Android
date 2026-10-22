// ANDROID-ONLY
package app.topgun.android.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives Firebase Cloud Messaging pushes (iOS `AppDelegate` + `MessagingDelegate`) and shows a basic notification.
 * Needs `google-services.json` and the google-services Gradle plugin at runtime; the class itself compiles without them.
 * Registered in the manifest (docs/reports/entry-manifest-needs.md). The FCM token is not logged or stored: the backend
 * has no device endpoint yet (docs/BACKEND_GAPS.md #17).
 */
class TopGunMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // TODO(port): send the token to the backend once a device-registration endpoint exists. Never log it.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: applicationInfo.loadLabel(packageManager).toString()
        val body = message.notification?.body ?: message.data["body"] ?: return
        show(this, title, body)
    }

    companion object {
        const val CHANNEL_ID = "topgun_general"

        /** Creates the channel (API 26+; minSdk is 26, so no version check is needed) and posts the notification. */
        fun show(context: Context, title: String, body: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Top Gun news", NotificationManager.IMPORTANCE_DEFAULT),
            )

            // Android 13+: without the runtime permission the notification would be dropped anyway.
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pending = launch?.let {
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(context.applicationInfo.icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
