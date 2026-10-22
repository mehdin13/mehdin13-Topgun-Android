# Manifest / Gradle needs of the geo work (map and globe)

No changes required beyond what already exists. Checked, not built:

- Gradle: `libs.maps.compose` and `libs.play.services.maps` are already in `app/build.gradle.kts`. `MapUi.kt` uses `GoogleMap`, `Marker`, `MapUiSettings`, `rememberCameraPositionState`, `rememberMarkerState(key, position)`; `rememberMarkerState` with a `key` parameter needs maps-compose 4.x or newer (check the version in `gradle/libs.versions.toml`; with 2.x/3.x drop the `key =` argument).
- Manifest: the `com.google.android.geo.API_KEY` meta-data (manifest placeholder `mapsApiKey`) is already declared. The key must have "Maps SDK for Android" enabled and the app's package and SHA-1 registered, otherwise the flat map shows a blank grid.
- No location permission is needed (no "my location" layer).
- The globe uses `res/drawable-nodpi/earth_texture.jpg` (already imported). No SceneView / Filament is used for it.
