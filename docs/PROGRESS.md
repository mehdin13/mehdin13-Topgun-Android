# Progress log

## Session 1 (2026-10-22): scaffold, core, design system, first screens

### Done
- Gradle project: `:app` (Compose, Material 3) + `:core` (pure Kotlin/JVM). Wrapper 8.10.2, AGP 8.7.3, Kotlin 2.0.21, version catalog.
- `:core` (compiled and tested outside the Android SDK: 31 tests, 0 failures):
  - `ApiError` (400/401/402/403/404/409/413/429/500/502/503 mapping, DRF `detail`/`error`/field messages, friendly `userMessage`, `Retry-After`).
  - `ApiClient` (OkHttp): token header, 401 signs the session out for authenticated requests, no retries, upload progress, per-request read timeout, `verifyToken` that never touches the session.
  - DTOs: `StoryDto`, `StorySummaryDto`, `PublicStoryDto` (no painting field), `GenerationQuotaDto`, `FavoriteResponseDto`, `UserDto`; list-or-DRF-page decoder; unknown enum values -> `UNKNOWN`; DRF datetimes.
  - `MediaUrlResolver`, `ApiConfiguration`, `SessionStore`, `TokenStore`.
  - `StoryApi`: my stories, story, public (sort/category/search), most-favorited, shared, my_collection, my_favorites, quota, favorite/unfavorite, save/unsave, rate, share/unshare, me.
- `:app` design system: palette generated from `tokens.json` (48 tokens x 4 themes incl. increased contrast via Android 14 contrast), spacing/radius/size/layout/motion tokens, type scale, `DreamlandTheme` (+ Material 3 color scheme/typography/shapes), components listed in `CLAUDE.md`.
- `:app` screens: Sign-in (Google/Apple buttons not wired; debug token sign-in), Home (your stories, quota, most favorited, recent), Discover (search, category chips, sort, grid, favorite), Library (My stories / Saved / Favorites), Story detail (cover, text, favorite, save, share/unshare for owner, rate, audio/PDF open in system app, owner-only painting), Profile (sign out), tab shell with center Create button.

### NOT verified
- **`:app` has never been compiled.** The session's network policy blocked `dl.google.com` (Android SDK and Google's Maven: AGP, Compose, AndroidX). Expect compile errors on the first build. Fix them first next session (open in Android Studio, or allow the host).
- No UI/instrumentation tests, no previews run on a device.

### Open TODOs (next session, in order)
1. First `./gradlew :app:assembleDebug`; fix compile errors; add `@Preview`s with fixture data (CLAUDE.md of iOS requires one per view).
2. Add the font files (Young Serif for display; a rounded UI face; a reading serif) if licensed; typography falls back to system serif/sans.
3. Real sign-in: Credential Manager (Google) needs the OAuth web client ID and the backend exchange endpoint (BACKEND_GAPS #1, #12). Apple on Android needs a web flow.
4. Create flow (idea, voice, painting) + Making/reveal screen: port from iOS `Features/Create`, `Paint`, `Making`. Multipart POST `stories/` and `stories/from-image/`; long read timeout; never auto-retry; handle 402/429/503/413.
5. Offline reading with Room (saved stories + files); offline banner.
6. Audio player (Media3) and PDF viewer in-app; per-output pending/failed states.
7. Welcome/onboarding, Grown-ups, parent gate, account deletion (backend gap #2), characters (#10, #21).
8. Tablet/foldable layouts (adaptive grids, navigation rail).
9. Image caching and cover placeholders per art style; Dynamic Type/TalkBack audit; 44dp audit.
10. `network_security_config.xml` allows cleartext to `10.0.2.2`/`localhost` in all builds; move it to a debug-only source set.

### Decisions and deviations
- Package/applicationId `com.mehdin13.dreamland` is a placeholder; confirm the real id before Play Console.
- Buttons are capsules (as the iOS code ships) although tokens.json says `radius-md` for buttons.
- Most-favorited uses `stories/public/most-favorited/` (backend path, BACKEND_GAPS #6), not the spec's `stories/most-favorited/`.
- `my_favorites/` does not exist on the backend yet (#43): a 404 shows an empty Favorites tab.
- `DELETE .../save/` returns 204 *with a body* on the backend (#47); OkHttp treats that as a protocol error and surfaces `ApiError.Network`. Fix on the backend, or handle it before shipping save/unsave.
- Fixtures/example JSON for `docs/api/examples` were not created: no live backend was reachable.
