# Progress log

## Session 1 (2026-10-22)

An earlier part of this session wrongly built a "Dreamland" app here (wrong project); that code was removed. TopGun work below.

### Foundation (done, verified by `core` tests and `tools/compose-check`)
- `:core`: all TopGun endpoints (`TopGunApi`), models, `ApiClient` (token, 401 sign-out, GET retry on 429/503), `MediaUrlResolver`, `AircraftFilter` (same form fields as iOS). 32 unit tests.
- `:app` design system from the iOS asset catalog + `Utils.swift`: palettes (light/dark), brand colors, tokens, type scale (Sailec/Futura/MonaKo copied from the iOS repo into `res/font`), theme, `TGImage`, state views, `TagChip`, `AircraftListCard`.
- App shell: `TopGunRoot` navigation graph (Intro -> Splash -> Main tabs; sign-in, paywall, details, countries, manufacturers, lists, blog, badges), floating tab bar, `AppContainer`, `UserRepository`, `AppPreferences`.
- `platform/` interfaces for all Android-only features; Android entry points (`TopGunApp`, `MainActivity`, token store, prefs).
- `tools/compose-check`: JVM type-check of the Compose code (the Android SDK is unreachable from the cloud session).

### Screens
Stubs with final signatures exist for every screen (see `features/`); they are being ported from the iOS views.

### Not verified
- No Gradle Android build has run (`dl.google.com` blocked). `// ANDROID-ONLY` files are unchecked.
