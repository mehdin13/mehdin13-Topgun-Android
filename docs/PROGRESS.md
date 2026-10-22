# Progress log

## Session 1 (2026-10-22)

An earlier part of this session wrongly built a "Dreamland" app here (wrong project); that code was removed. TopGun work below.

### Foundation (done, verified by `core` tests and `tools/compose-check`)
- `:core`: all TopGun endpoints (`TopGunApi`), models, `ApiClient` (token, 401 sign-out, GET retry on 429/503), `MediaUrlResolver`, `AircraftFilter` (same form fields as iOS). 32 unit tests.
- `:app` design system from the iOS asset catalog + `Utils.swift`: palettes (light/dark), brand colors, tokens, type scale (Sailec/Futura/MonaKo copied from the iOS repo into `res/font`), theme, `TGImage`, state views, `TagChip`, `AircraftListCard`.
- App shell: `TopGunRoot` navigation graph (Intro -> Splash -> Main tabs; sign-in, paywall, details, countries, manufacturers, lists, blog, badges), floating tab bar, `AppContainer`, `UserRepository`, `AppPreferences`.
- `platform/` interfaces for all Android-only features; Android entry points (`TopGunApp`, `MainActivity`, token store, prefs).
- `tools/compose-check`: JVM type-check of the Compose code (the Android SDK is unreachable from the cloud session).

### Screens (all ported from the iOS views; whole app passes `tools/compose-check/check.sh`)
Home (search, categories, filter sheet, featured cards, Load More, long-press), Aircraft detail (specs, gallery, video, PDF, 3D/AR, comments, rating, VIP lock), Countries (list/map/globe/detail), Manufacturers, curated lists, Favorites, Settings (+ appearance), Blog, Badges, Intro, Splash, Sign-in (Google), Paywall. Per-area reports with differences from iOS: `docs/reports/*.md`.

### Not verified
- No Android Gradle build has run (`dl.google.com` blocked in the cloud session). `// ANDROID-ONLY` files (video, SceneView/AR, maps/globe, PDF, Google sign-in, RevenueCat, Lottie, FCM) are written against library APIs from memory and are the most likely to need fixes.
- Nothing has run on a device; layout was matched from the Swift source, not compared visually.

### Next steps
1. Open in Android Studio (or allow `dl.google.com`) and fix compile errors in the ANDROID-ONLY files, then `./gradlew :app:assembleDebug`.
2. Fill `local.properties` keys (see `docs/SETUP.md`); add `google-services.json` and re-enable the google-services plugin.
3. Ask the backend owner for glb 3D models, ID-token verification, account deletion and the fixes in `docs/BACKEND_GAPS.md`.
4. Runtime POST_NOTIFICATIONS request; offline model save; gallery save-to-photos; unit setting in preferences; `@Preview`s with a fixture container; tablet layouts.
5. Confirm the application id (`app.topgun.android`) and the Play listing URL.
