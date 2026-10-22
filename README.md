# Dreamland for Android

Jetpack Compose port of the Dreamland iOS storybook app. See `CLAUDE.md` for architecture and rules, `docs/PROGRESS.md` for status.

```
./gradlew :core:test           # API client, DTOs, errors (no Android SDK needed)
./gradlew :app:assembleDebug   # needs the Android SDK
```

Debug builds talk to `http://10.0.2.2:8000/api/` (the host machine's Django server from the emulator). Set URLs in `gradle.properties`.
