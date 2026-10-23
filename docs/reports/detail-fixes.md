# Detail screen fixes (tester report)

Verification: `tools/compose-check/check.sh` prints `compose-check: OK`; `:core:test` passes (incl. new glb decoding test). The `// ANDROID-ONLY` files (`VideoUi.kt`, `ModelUi.kt`) are NOT type-checked here; I checked the SceneView 2.2.1 API against its sources jar from Maven Central, but nothing was run on a device.

## 1. YouTube does not load
Root cause (likely, not reproducible without a device): the embed page was loaded with base URL `https://www.youtube.com` (the player's own origin), no `origin=` parameter and no `referrerpolicy`; YouTube then answers error 153 ("player configuration error"). It also loaded the WebView immediately with autoplay needing a gesture, and the id parser missed some link forms.
Changes:
- `platform/ui/VideoUi.kt` `YouTube()`: thumbnail (`img.youtube.com/vi/<id>/hqdefault.jpg`) + play button; the WebView is created only on tap. Host page is `loadDataWithBaseURL("https://api.topgunapp.com/", ...)` with `<iframe src="https://www.youtube.com/embed/<id>?playsinline=1&rel=0&autoplay=1&modestbranding=1&origin=https://api.topgunapp.com" referrerpolicy="strict-origin-when-cross-origin">`, JS + DOM storage on, third-party cookies on, no user-gesture requirement for playback. Main-frame load errors flip back to the thumbnail with a message.
- New pure Kotlin `platform/ui/YouTubeLinks.kt`: `youTubeVideoId()` (watch?v= with any param order, youtu.be, embed, shorts, live, v, m./music./www. hosts, youtube-nocookie, no scheme, bare id) and `youTubeThumbnailUrl()`.
- `features/details/DetailsMain.kt` `VideoSection`: always shows a "Watch on YouTube" outlined button (48dp) via `platform.openUrl` with a normalised `https://www.youtube.com/watch?v=<id>` (stored link if no id).
Unverified: whether error 153 is really gone on a device (the fallback button covers the failure case). In-page player errors cannot be detected without the IFrame API, so they show YouTube's own message inside the player. Fullscreen button is intentionally absent (no `allowfullscreen`; no custom-view handling).

## 2. Too many gallery dots
- New `designsystem/components/SlidingDotsIndicator.kt`: `SlidingDotsIndicator(pageCount, position)` draws max 5 dots on a Canvas; selected dot larger + accent, edge dots shrink only where more pages continue, window slides continuously with `currentPage + currentPageOffsetFraction`. Also `PageCounterPill(current, total)` ("3 / 24"). One TalkBack description for the whole indicator.
- Used in the card-deck gallery (`DetailsMain.kt` CardDeck) and the pill on the full-screen gallery (`DetailsPages.kt`; that screen uses a thumbnail strip, not dots). Tokens added to `TGDetailSize` (dotSize, dotGap, dotWindow, dotSelectedScale, dotEdgeScale, playButton), marked Android-only.
Unverified visually (math checked by reading only).

## 3. 3D models
- `core` `Models.kt`: `fileGlb` (`@JsonNames("file_3D_glb","file_3d_glb")`) on `Aircraft` and `AircraftDetail`; test `decodesGlbFieldUnderBothSpellings` + assertions in the existing tests.
- Details: 3D tile shows if `fileGlb` or `file3d` exists; `ModelPage` prefers `fileGlb`, falls back to `file3d` only if it is glb/gltf, otherwise the existing "not available on Android yet" card. New "Open in 3D viewer" button (Google Scene Viewer URL `https://arvr.google.com/scene-viewer/1.0?file=<encoded>&mode=3d_preferred&title=...` through `platform.openUrl`) always shown with a supported model. Needs an https, publicly reachable glb URL.
- `ModelUi.kt` real bugs found and fixed:
  1. The camera manipulator was created without `orbitHomePosition`; it overwrites the camera every frame, so the camera started at the manipulator default (about 1 unit away) regardless of `cameraNode.position` -> model huge / clipped. Now orbit home (0,0,2.2) and target origin are passed.
  2. Model was not centred (`scaleToUnits` only scales); now scaled to 1 unit and shifted by its scaled bbox centre so orbit is around the aircraft.
  3. File read + parse ran synchronously inside `remember` on the main thread during composition and a failure silently produced blank/`null`. Now the file is read on IO, validated (glb magic `glTF`; a bad download such as an HTML page is deleted from the cache), the Filament instance is created on the main thread in `produceState` inside try/catch, and loading / error states show text (tokens) instead of a blank view.
  4. Same loading path used for AR.
- API facts checked in the 2.2.1 sources: `ModelLoader.createModelInstance(Buffer|File|String|Int)`, `Scene(...)` parameters, `rememberCameraManipulator(orbitHomePosition, targetPosition)`, default environment asset `environments/neutral/neutral_ibl.ktx` exists in the AAR, `ModelNode(modelInstance, autoAnimate, scaleToUnits, centerOrigin)`, `ModelNode.center`.
- `ModelCache.kt` reviewed, unchanged (download is atomic via .part rename; failures return null).
Unverified: nothing was compiled against the real AARs or run on a device (the lead's CI is the compile check); the backend `file_3D_glb` field name is assumed and both spellings are accepted; the camera distance 2.2 and the lighting (default neutral IBL + main light, black skybox) were not looked at on screen.
