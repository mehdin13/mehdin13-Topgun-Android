# Top Gun for Android

Android port of the Top Gun iOS app ("Top Gun – Aviation Academy": an encyclopedia of military aircraft with 3D models and AR, galleries, PDFs, videos, countries and manufacturers on a map/globe, curated lists, favorites, comments, ratings, blog, badges, and a Pro subscription). Same screens, flows, colors, fonts and API as iOS; Jetpack Compose + Material 3 underneath.

## Source of truth
- iOS app: `mehdin13/topgun` (cloned at `/home/user/TopGun`, Swift sources in `Top Gun/`). Port screen by screen from the SwiftUI view, its ViewModel and `Utility/NetworkManager .swift`.
- Backend: `ncodestudio/topgun-backend` (Django REST, cloned at `/home/user/TopGun-Backend`). API root `https://api.topgunapp.com/api/v1/`. Read its serializers/views to confirm shapes. **Do not edit the backend**; log problems in `docs/BACKEND_GAPS.md`.
- `docs/PROGRESS.md` is the build log: read it first, update it last.
- (An earlier session wrongly built a "Dreamland" app in this repo. It is unrelated; ignore `dreamland-ios` and that history.)

## Modules
- `:core` pure Kotlin/JVM, no Android types. `TopGunApi` (all endpoints), models (kotlinx-serialization, snake_case, lenient), `ApiClient` (OkHttp, token header, 401 -> sign-out, GET retry on 429/503), `MediaUrlResolver`, `SessionStore`. Unit-tested: `./gradlew :core:test`.
- `:app` Compose UI. Package `app.topgun.android`:
  - `designsystem/` colors (`TG.colors`, `TGBrand`), tokens (`TGSpace`, `TGRadius`, `TGSize`), type (`TGText`), theme, `components/`.
  - `data/` `AppContainer` (pure Kotlin DI), `UserRepository`, `AppPreferences`, `Load<T>`, `containerViewModel`.
  - `platform/` pure-Kotlin interfaces for everything Android-only (`PlatformServices`, `PlatformUi`). Android implementations live in files whose FIRST LINE is `// ANDROID-ONLY`.
  - `navigation/` routes + `TopGunRoot` + tab bar.
  - `features/<name>/` one package per screen group: `XScreen` composable + `XViewModel`.

## Rules
- MVVM: `ViewModel` + `StateFlow`; collect with `collectAsStateWithLifecycle()`. No LiveData/RxJava. Use `Load<T>` for loading/loaded/failed. Errors shown to people come from `ApiError.userMessage`.
- Use `TG.colors`, `TGBrand`, `TGSpace/TGRadius/TGSize`, `TGText` for every color, size, spacing and font. No hardcoded hex/dp/sp in screens; if a value is missing, add it to the token file with the iOS source named in a comment.
- Images only through `TGImage(path, ...)` (resolves relative `/media/...` paths). Never log tokens or personal data. Token lives only in `EncryptedTokenStore`.
- Anything needing an Android-only library (ExoPlayer, SceneView/ARCore, Google Maps, Credential Manager, RevenueCat, Lottie, PdfRenderer, Firebase) goes behind `PlatformUi` / `PlatformServices` with the implementation in an `// ANDROID-ONLY` file. Everything else must be plain Compose so it can be type-checked.
- Accessibility: 48dp minimum touch targets, `contentDescription` on every informative icon/image, text in `sp`.
- English-only UI. Use `Modifier.testTag` sparingly; no `stringResource` yet (strings are literals, as in iOS).
- Every screen composable gets a `@Preview` with fixture data when practical (fixtures in the same package under `Fixtures.kt`, never in `main` data paths used at runtime).

## Verifying (important)
- The Claude cloud session cannot reach `dl.google.com`, so the Android SDK/AGP/AndroidX cannot be downloaded there. Use the JVM type-check instead:
  `tools/compose-check/check.sh` (needs Maven Central only; prints `compose-check: OK` or the Kotlin errors). It compiles `core/` + all non-`ANDROID-ONLY` app sources + shims for Android APIs in `tools/compose-check/shims/` (extend a shim if you use a new Android/AndroidX API in checked code; keep shims minimal and signature-compatible).
- `// ANDROID-ONLY` files and `AndroidManifest.xml`/Gradle files are NOT checked: review them by eye. The first real `./gradlew :app:assembleDebug` (Android Studio or CI) may still surface errors there.
- `:core` tests: `./gradlew :core:test` (or the scratch JVM build used in the cloud session).
- Run `check.sh` before every commit that touches `app/`.

## API facts that bite
- Pagination `{count,next,previous,results}`; `page`, `page_size` (max 100). Throttle ~4 requests/second per user: do not fire many parallel requests; `ApiClient` retries GETs on 429.
- Token auth `Authorization: Token <key>`; tokens expire after 180 days (401 -> sign out).
- Media fields are relative `/media/...` (sometimes absolute); `file_3D` is `.usdz` (Apple only): see `docs/BACKEND_GAPS.md`.
- VIP aircraft (`is_vip`) return 403 on detail unless the user is Pro; lists still include them (show a lock).
- Favorites list items come back with `is_favorite=false` (backend bug): treat everything in the favorites list as favorite.
