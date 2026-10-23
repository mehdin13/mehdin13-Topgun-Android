# Maps / countries fixes

## 1. Crash while scrolling the countries list
Root cause (by code reading, not reproduced on a device): `country/` is ordered by `-aircraft_count` with no tie-break, so page N+1 can repeat a country from page N. `CountryListViewModel.loadMore` appended raw results and `LazyVerticalGrid` used `key = country.id` -> duplicate key -> IllegalArgumentException.
Changes:
- `CountryListViewModel`: first page, appended pages and the all-countries list for the map are de-duplicated by id (order kept). `loadMore` also skips ahead to the next page when a page adds nothing new (otherwise pagination would stall).
- `CountryListScreen`: grid de-duplicates defensively; the "last item -> load more" trigger uses that list.
- `distinctBy { id }` before every other `key = { it.id }` in `countries/CountryDetailScreen`, `manufacturers/ManufacturerListScreen`, `manufacturers/ManufacturerDetailScreen`, `lists/CustomAircraftListScreen`.
- Map/globe pins ignore null, non-finite or out-of-range coordinates and duplicate ids.
- Map/globe body is wrapped in `Modifier.weight(1f)` so the map always has a bounded height.

## 2. Globe zoom
`MapUi.Globe` rewritten: pinch zoom 1x-5x, drag = yaw + tilt (pitch clamped +-80 degrees), double tap resets zoom and tilt, auto-spin kept (paused while a finger is down). The sphere is rendered by inverse orthographic projection on `Dispatchers.Default` (`snapshotFlow` + `collectLatest`, frames dropped when busy) at 256 px idle / 144 px while gesturing. Pins use the same projection (yaw + pitch + zoom), so they stay aligned and tappable; far-side pins are moved off-screen; the box is clipped. Does not use Google Maps.

## 3. Google map blank everywhere
Verified by reading (all correct, nothing changed): `AndroidManifest.xml` has `<meta-data com.google.android.geo.API_KEY value="${mapsApiKey}">` inside `<application>`; `app/build.gradle.kts` sets `manifestPlaceholders["mapsApiKey"]` once in `defaultConfig` from `local.properties` `topgun.mapsApiKey` (trimmed); CI writes `topgun.mapsApiKey=$MAPS_API_KEY`. maps-compose 6.2.1 API use (`GoogleMap`, `Marker`, `rememberMarkerState`, `MapUiSettings`, `onMapLoaded`) is correct; layouts give the map a bounded size.
Most likely cause is therefore outside the code: Maps SDK for Android not enabled, no billing account, or key restriction not containing package + the SHA-1 of the installed APK, or an empty/quoted `MAPS_API_KEY` secret.
Changes in `MapUi.World`:
- Reads the manifest meta-data at runtime; blank -> "Google Maps key missing..." message (new `designsystem/components/MapNoticeBox.kt`).
- Spinner until `onMapLoaded`; after 12 s without it -> the "The map could not be loaded. Check the Maps API key restrictions (package app.topgun.android + signing SHA-1) and that Maps SDK for Android is enabled." message.
- Gestures/zoom explicitly enabled, invalid pins filtered.
- `docs/CI_SETUP.md`: troubleshooting section (enable SDK, billing, SHA-1 FB:BF:...:00, Logcat "Authorization failure" / "API key not found").
Top Countries still reaches grid / map / globe from the same three top-bar buttons; the globe works even when Google Maps is blank.

## Unverified
- Nothing ran on a device. `tools/compose-check` passes, but `MapUi.kt` is ANDROID-ONLY (not type-checked); the CI build is the real compile check.
- Gesture feel, performance of the 256 px render on real phones, and tilt direction need a visual check.
- With a rejected key Google's SDK may still fire `onMapLoaded` (grey grid, no tiles); then the timeout message will not show and Logcat is the only signal.
- `build.gradle.kts` (not mine) trims whitespace but not surrounding quotes in `topgun.mapsApiKey`.
- The duplicate-key diagnosis is from code, not a captured stack trace.
