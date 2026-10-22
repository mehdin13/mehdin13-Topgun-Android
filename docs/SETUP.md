# Setup: keys and sign-in

Secrets never go into source control. Put them in `local.properties` (git-ignored) at the repo root, or pass them as Gradle
properties on CI. `app/build.gradle.kts` turns them into `BuildConfig` fields.

```properties
# local.properties
sdk.dir=/path/to/Android/sdk
topgun.googleWebClientId=1234567890-abcdefg.apps.googleusercontent.com
topgun.revenueCatKey=goog_xxxxxxxxxxxxxxxxxxxx
topgun.mapsApiKey=AIza...
```

| Key | Used for | Empty value |
|---|---|---|
| `topgun.googleWebClientId` | Google sign-in (`BuildConfig.GOOGLE_WEB_CLIENT_ID`) | Sign-in shows "Google sign-in didn't work" |
| `topgun.revenueCatKey` | RevenueCat Google Play public SDK key (`BuildConfig.REVENUECAT_API_KEY`) | No purchases; the paywall shows "Purchases aren't available in this build." |
| `topgun.mapsApiKey` | Google Maps (manifest placeholder) | Map is blank |

## Google sign-in (Credential Manager)

1. Google Cloud console -> the Top Gun project -> APIs & Services -> OAuth consent screen: configure it (app name, support e-mail).
2. Credentials -> Create credentials -> OAuth client ID -> **Android**:
   - package name `app.topgun.android`
   - SHA-1 of the signing key. Debug key: `./gradlew :app:signingReport` (or `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`).
   - Add one Android client per signing key: debug, release/upload key, **and Play App Signing key** (Play console -> App integrity -> App signing).
3. Credentials -> Create credentials -> OAuth client ID -> **Web application**. Its client ID is the value for
   `topgun.googleWebClientId`. (The Android client ID itself is never put in the app; Credential Manager matches the app
   by package + SHA-1, and the *web* client ID is passed as `serverClientId`.)
4. Test accounts: while the consent screen is in "Testing", add the Google accounts under Test users.
5. The iOS client ID (`635030212606-...`) cannot be reused.
6. Backend: once it verifies Google ID tokens (docs/BACKEND_GAPS.md #1), the web client ID must be an allowed audience.
   Today the app sends only e-mail and names.

## RevenueCat

1. RevenueCat dashboard -> Project -> add an **Android (Google Play)** app, link the Play service account credentials.
2. Create the Play subscription products and attach them to the existing entitlement **`Pro`**.
   Google Play ids must be lowercase, so they cannot equal the iOS ids. The app maps by name: an id containing
   `6month` = 180 days, `12month`/`annual`/`yearly` = 360, `lifetime` = 9999 (exact iOS ids lowercased also work:
   `topgun_499_6month_2w0`, `topgun_999_12month_2w0`, `topgun_2999_lifetime`).
3. Build an offering with a Paywall (Paywalls editor) and make it the current offering.
4. Copy the Android public SDK key (`goog_...`) to `topgun.revenueCatKey`.
5. Test with a license-tester account on an internal-testing track build.

## Firebase Cloud Messaging

1. Firebase console -> add an Android app `app.topgun.android` -> download `google-services.json` to `app/`.
2. Re-enable the google-services plugin (see docs/reports/entry-manifest-needs.md).
3. There is no device-token endpoint on the backend yet (docs/BACKEND_GAPS.md #17).
