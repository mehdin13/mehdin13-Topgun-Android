# Build the test APK with GitHub Actions (walkthrough)

Workflow: `.github/workflows/android.yml`. It runs the `core` tests, builds `app-debug.apk` and uploads it as an artifact; when started by hand with **publish** ticked it also creates a GitHub *pre-release* with the APK attached (a plain download link you can open on the phone).

Nothing here needs Android Studio, except step 1 if you prefer its terminal for `keytool`.

## 0. Rotate what was pasted in chat
The Gmail app password and the RevenueCat **secret** key (`sk_...`) were pasted in a chat, and the backend repo tracks its `.env`. Treat both as leaked: create a new Gmail app password, create a new RevenueCat secret key, update the server `.env`, and (when convenient) stop tracking `.env` in git. The iOS RevenueCat key (`appl_...`) is a public SDK key and is fine to keep in the app.

## 1. Create a signing key for test builds (once)
Google sign-in only works if the APK is signed with a key whose SHA-1 is registered in Google Cloud. GitHub runners would otherwise create a new random debug key every run, so use one fixed key:

```bash
keytool -genkeypair -v -keystore topgun-test.keystore -alias topgun -keyalg RSA -keysize 2048 -validity 10000
# answer the questions, remember the two passwords
keytool -list -v -keystore topgun-test.keystore -alias topgun | grep -E "SHA1|SHA256"   # note the SHA1
base64 -w0 topgun-test.keystore > keystore.b64        # macOS: base64 -i topgun-test.keystore -o keystore.b64
```
Keep `topgun-test.keystore` somewhere safe (not in git). Losing it only means registering a new SHA-1.

## 2. Google Cloud (project number 635030212606, the one that owns the iOS client)
1. APIs & Services -> **OAuth consent screen**: make sure it is configured (app name Top Gun, support email). Publish it (or add yourself as a test user).
2. APIs & Services -> Credentials -> **Create credentials -> OAuth client ID -> Web application**, name "Top Gun Android server". Copy its Client ID (`....apps.googleusercontent.com`). This is `GOOGLE_WEB_CLIENT_ID` below. The iOS client ID cannot be used for this.
3. Create credentials -> OAuth client ID -> **Android**: package `app.topgun.android`, SHA-1 from step 1. (Add a second one later for the Play Store signing key.)
4. Library -> enable **Maps SDK for Android**, then Credentials -> Create API key; restrict it to Android apps (package `app.topgun.android` + the same SHA-1) and to the Maps SDK. This is `MAPS_API_KEY`.

## 3. Backend (ncodestudio/topgun-backend)
Add to the server `.env` (the iOS and web client IDs, comma separated), install `google-auth`, redeploy:
```
GOOGLE_OAUTH_CLIENT_IDS=635030212606-97thkbc13pbmjarg7o88suh05c7892id.apps.googleusercontent.com,<the WEB client id from step 2>
ALLOW_UNVERIFIED_SOCIAL_LOGIN=True
```
Keep `ALLOW_UNVERIFIED_SOCIAL_LOGIN=True` only until the iOS app also sends the Google `id_token`; then set it to `False` (otherwise anyone can sign in as any email).
Account deletion is `DELETE https://api.topgunapp.com/api/v1/users/delete_account/` (needs the user's token). Google Play also asks for a public web page that explains how to delete the account and data; link it in the Play Console.

## 4. RevenueCat
The `appl_...` key is for iOS only. In RevenueCat: Project settings -> Apps -> **+ New** -> Google Play (package `app.topgun.android`), upload the Play service-account JSON, then copy that app's **public SDK key** (starts with `goog_`). This is `REVENUECAT_ANDROID_KEY`. Create the same entitlement `Pro` and attach the Google Play products (ids in lower case: `topgun_499_6month_2w0`, `topgun_999_12month_2w0`, `topgun_2999_lifetime`) to it and to an Offering.

## 5. Firebase (push notifications, optional)
Firebase console -> your project -> Add app -> Android -> package `app.topgun.android` -> download `google-services.json` -> `base64 -w0 google-services.json` -> secret `GOOGLE_SERVICES_JSON_BASE64`. Without it the app builds and runs, only push is off.

## 6. Add the GitHub secrets
GitHub -> repository `mehdin13/mehdin13-Topgun-Android` -> **Settings -> Secrets and variables -> Actions -> New repository secret**. Add these (missing optional ones just switch that feature off):

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | content of `keystore.b64` (step 1) |
| `ANDROID_KEYSTORE_PASSWORD` | the keystore password |
| `ANDROID_KEY_ALIAS` | `topgun` |
| `ANDROID_KEY_PASSWORD` | the key password |
| `GOOGLE_WEB_CLIENT_ID` | web client ID (step 2.2) |
| `MAPS_API_KEY` | Maps key (step 2.4) |
| `REVENUECAT_ANDROID_KEY` | `goog_...` (step 4) |
| `GOOGLE_SERVICES_JSON_BASE64` | optional (step 5) |

Also check **Settings -> Actions -> General -> Workflow permissions** is "Read and write" (needed only to publish the pre-release).

## 7. Run it and install the APK
Actions tab -> **Android build** -> **Run workflow** (tick *publish*). When it is green:
- open the run -> Artifacts -> `topgun-apk`, or
- open **Releases** -> the newest "Top Gun test build" -> download `app-debug.apk` on the phone and install it (allow "install unknown apps" for the browser).
The log step "Print the signing certificate SHA-1" shows the SHA-1 to register if Google sign-in says "no credentials available".

## Local builds in Android Studio
Open the repo, create `local.properties` (not committed) next to `gradlew`:
```
sdk.dir=/path/to/Android/sdk
topgun.googleWebClientId=<web client id>
topgun.revenueCatKey=<goog_ key>
topgun.mapsApiKey=<maps key>
```
Android Studio signs debug builds with its own debug key: register that SHA-1 too (`./gradlew signingReport`). Debug and release builds talk to the live API `https://api.topgunapp.com/api/v1/`. To use a local Django server instead, add `topgun.apiBaseUrl=http://10.0.2.2:8000/api/v1/` (emulator) or `http://<your computer's LAN IP>:8000/api/v1/` (phone, needs `usesCleartextTraffic` for that host) to `local.properties`.

## Troubleshooting: Google maps are blank or grey
Symptoms: the flat map on "Top Countries" or the operators map on an aircraft shows nothing, only the grey grid, or after about 12 seconds the in-app message "The map could not be loaded...". (The globe does not use Google Maps and keeps working.)

1. **"Google Maps key missing" in the app**: the build had no `topgun.mapsApiKey`. In CI check that the secret `MAPS_API_KEY` exists (no quotes, no spaces) and rebuild; locally add `topgun.mapsApiKey=<key>` to `local.properties`.
2. **Enable the API**: Google Cloud console -> APIs & Services -> Library -> **Maps SDK for Android** -> Enable (not only "Maps JavaScript API" or "Places").
3. **Billing**: the Google Cloud project must have a billing account linked (Maps has a free monthly credit, but without billing the map stays blank).
4. **Key restrictions**: APIs & Services -> Credentials -> your key -> Application restrictions = *Android apps* must list package `app.topgun.android` with the SHA-1 of the certificate that signed the installed APK. The CI-signed APK is `FB:BF:FA:F7:6A:A1:42:E3:D5:D2:24:D6:20:65:82:55:09:F6:85:00` (a local Android Studio debug build has a different SHA-1: `./gradlew signingReport`). API restrictions must include **Maps SDK for Android**. Changes can take a few minutes to apply.
5. **Read Logcat** (filter `tag:Google` or search for `Maps`) while opening the map:
   - `Authorization failure. Please see https://developers.google.com/maps/documentation/android-sdk/start` followed by `Ensure that the "Maps SDK for Android" is enabled` and `Ensure that the following Android Key exists: API Key: ... Android Application (<cert_fingerprint>;<package>): ...` -> the SHA-1 or package in that line is not in the key restriction (add exactly that pair), or the SDK is not enabled.
   - `API key not found. Check that <meta-data android:name="com.google.android.geo.API_KEY" android:value="API_KEY"/> is in the <application> element of AndroidManifest.xml` -> the key was empty at build time (see 1).
   - `This API project is not authorized to use this API` / `BillingNotEnabledMapError` -> steps 2 and 3.
