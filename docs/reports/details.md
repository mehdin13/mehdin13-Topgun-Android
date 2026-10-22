# Aircraft Detail port (details worker)

Live iOS view: `View/DetailsView.swift` (`DetailsView 2.swift` is not in the Xcode target). Specs tables are in the root `SpecialDetailView.swift`.

## Ported (`features/details/`)
- `AircraftDetailScreen.kt` (same signature): state host, loading / error / VIP-locked states, toast, sub-page switching with Back handling. Pushed iOS pages (gallery, specification, videos, operators map, PDF, 3D, comments) are in-place sub-pages (`DetailPage`), Back returns to the detail.
- `DetailsMain.kt`: header (400dp image, back/share/favorite), title pills, like button, quick-action tiles, overview (800-char / first-paragraph logic), trivia, general details grid, portrait card deck, YouTube, operators (flag grid -> `onOpenCountry`, small `WorldMap`, info button -> full map), manufacturers (-> `onOpenManufacturer`), variants and similar (`AircraftListCard`, lock for VIP when not Pro), Wikipedia, 5-star rating (`api.rate`, sign-in dialog), "Show Comments".
- `DetailsPages.kt`: zoomable gallery (pager, pinch/double-tap, thumbnails, tap hides chrome), full-screen deck viewer, specification page (iOS rows + unit conversion + sketches + extra fields + `customFields`), video queue (`PlatformUi.VideoPlayer`), PDF, 3D page and AR.
- `CommentsPage.kt` / `CommentsViewModel.kt`: list, header with average, post (sign-in dialog), reload after posting, "User <id>" / "You" (own id from prefs), time ago.
- `DetailsViewModel.kt`, `SpecFormat.kt` (pure unit conversions).
- Design system: `designsystem/components/DetailKit.kt` (DetailCircleButton, DetailPill, DetailHeading, DetailActionTile, DetailInfoCell, DetailStars); tokens appended: `TGDetailColors` (TGColors.kt), `TGDetailSize`, `TGDetailSpace` (TGTokens.kt) with iOS sources in comments.
- ANDROID-ONLY: `platform/ui/VideoUi.kt` (Media3 + YouTube WebView embed), `ModelUi.kt` (SceneView 2.2.1 viewer, ARScene placement), `PdfUi.kt` (PdfRenderer), `platform/ModelCache.kt` (cacheDir/models, `info()/clear()/fetch()`).
- Manifest entries needed: `docs/reports/details-manifest-needs.md`.

Request order: detail first; comments only when the Comments page opens (no bursts). Favorite/like/rate/comment need sign-in, else `onShowSignIn()`.

## Differences from iOS (and why)
- 3D: backend ships `.usdz`. Viewer and AR only for `.glb/.gltf` URLs; otherwise a card "3D model not available on Android yet". Today this means the 3D tile always leads to that card.
- No "Save for offline / Remove offline model" buttons: `PlatformServices` has no per-model cache API and I may not edit it. The Android viewer caches every model automatically; Settings > Clear Cache removes them.
- No "Save image to Photos" in the gallery (no platform method; needs MediaStore).
- Tiles are shown only when their content is non-empty (iOS shows Gallery/Operators tiles for empty arrays). Video tile uses the iOS "inactive" gradient as iOS does. "County operation" typo fixed to "Country operation".
- Flag tap opens the country directly (task spec) instead of iOS's 2-second tooltip. Flags come from the API `flag_icon`, then the bundled svg by code, then `aircraft_flag_icon`.
- Share text has no store link (no Play URL known). Favorite "pop" animation (Pow) not ported.
- Like button added under the title (iOS live view has none); count adjusts +/-1 from the toggle reply since the detail has no `is_liked`.
- Units toggle (Metric/Imperial) lives on the Specification page, default Metric; it is not yet bound to the Settings unit preference (no such preference in `AppPreferences`).
- `Developed From` is an id in the DTO: shown as "Aircraft #id" and opens that aircraft.
- Rating sign-in alert has an extra "Sign in" button. Material-less iOS blur cards replaced by solid dark card.
- No `@Preview`: `TGImage` needs `LocalAppContainer`, there is no fixture container yet.

## TODO
- Bind units to Settings; add `PlatformServices` methods for model offline save/remove and image save; replace the dark rating card with a blurred one.
- Backend: a glb file field (BACKEND_GAPS 15).

## Backend problems noticed
- Detail embeds `comments` (AircraftComment: email/first_name) but the list/post endpoint uses the older `Comment` model with `user` as a bare id, so names cannot be shown (already in BACKEND_GAPS 8).
- `aircraft_like` response has no total count and the detail has no `is_liked`, so the initial liked state is unknown.
- `developed_from` is an id (iOS treats it as a string).

## Unverified
- `check.sh` prints `compose-check: OK` for all checked code, but nothing has run on a device/emulator.
- The ANDROID-ONLY files are not compiled. Highest risk: SceneView/arsceneview 2.2.1 API names (`rememberNodes`, `rememberCameraNode`, `createModelInstance(Buffer)`, `ARScene` params, `rememberOnGestureListener`), reusing one `ModelInstance` for repeated AR placements, WebView YouTube embed behavior, Media3 imports.
- Layout/spacing is matched from the Swift source, not compared visually. Zoom gesture conflicts with the pager are untested.
