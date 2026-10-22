# Manifest / Gradle needs of the entry flow

I did not edit `AndroidManifest.xml` or any Gradle file. Apply these by hand.

## AndroidManifest.xml (inside `<application>`)

```xml
<!-- Firebase Cloud Messaging: shows pushes (platform/TopGunMessagingService.kt) -->
<service
    android:name=".platform.TopGunMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Optional: channel used for pushes that the system shows itself while the app is in the background -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="topgun_general" />
```

Already present and enough: `INTERNET`, `POST_NOTIFICATIONS`. `com.android.vending.BILLING` is merged in by the RevenueCat library.
`MainActivity` is a `ComponentActivity` (needed by Credential Manager and the RevenueCat paywall).

Credential Manager needs no manifest entries. If Play services is old on a device, `androidx.credentials-play-services-auth`
(already a dependency) provides the Google ID provider.

## Gradle

Nothing new is required: `libs.androidx.credentials`, `androidx.credentials.play`, `googleid`, `lottie.compose`,
`revenuecat.purchases`, `revenuecat.purchases.ui`, `firebase.messaging` are already in `app/build.gradle.kts`.

To turn Firebase on (until then the service is simply never invoked):
1. Put `google-services.json` for package `app.topgun.android` into `app/`.
2. In the root `build.gradle.kts` make sure the plugin is declared (`alias(libs.plugins.google.services) apply false`)
   and in `app/build.gradle.kts` re-enable `alias(libs.plugins.google.services)`.

## Proguard (release builds, `isMinifyEnabled = true`)

RevenueCat and Credential Manager ship consumer rules; no extra rules are known to be needed. Verify on the first release build.

## Runtime permission (not in my files)

Android 13+ needs the user to grant `POST_NOTIFICATIONS` (iOS asks at launch in `AppDelegate`). Nothing in the app requests it yet;
`TopGunMessagingService` silently skips the notification when it is not granted. Ask for it from `MainActivity` or the Settings
notification toggle (`PlatformServices.setNotificationsEnabled`).
