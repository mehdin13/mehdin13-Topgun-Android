# Geo port: countries, manufacturers, curated lists, map and globe

## Ported
| Android | iOS source |
|---|---|
| `features/countries/CountryListScreen.kt`, `CountryListViewModel.kt` | CountryListView, CountryListViewModel/FeaturedViewModel (pages of 20, load more at the last card), MapView toggle |
| `features/countries/CountryDetailScreen.kt`, `CountryDetailViewModel.kt` | CountryView (cover + round flag switching icon/flag every 5 s, name, capital, description, HIGHLIGHTS, filterable 2-row equipment grid, manufacturers carousel) |
| `features/countries/CountryMapPanel.kt` | MapView + GlassmorphicCalloutView ("More Info" opens the country) |
| `features/manufacturers/ManufacturerListScreen.kt`, `ManufacturerViewModels.kt` | ManufacturerListView/ManufacturerCard (`custom-manufacturer-list?name=Top manufacturers`) |
| `features/manufacturers/ManufacturerDetailScreen.kt` | ManufacturerView (logo, Wikipedia and website via `platform.openUrl`, description, filterable equipment grid) |
| `features/lists/CustomAircraftListScreen.kt`, `CustomAircraftListViewModel.kt` | CustomAircraftGridView (adaptive grid, 110dp min cell) |
| `designsystem/components/GeoComponents.kt` | FeaturedCardItem, EquipmentCard, cover/flag helpers (new shared components) |
| `platform/ui/MapUi.kt` (ANDROID-ONLY) | MapView flat map, GlobeView |

Tokens appended: `TGGeo` in `TGTokens.kt`, `TGGeoColor` in `TGColors.kt` (iOS sources named in comments). Screens signatures unchanged.

VIP rule on curated lists: `aircraft.isVip && !user.isPremium` shows the lock; tap -> `onShowPaywall()` if signed in, else `onShowSignIn()`.

## Map and globe approach
- Flat map: `maps-compose` `GoogleMap` + default `Marker` per `MapPin`; click returns `true` (own callout instead of the info window).
- Globe: **Compose Canvas orthographic projection**, not SceneView. `earth_texture.jpg` is sampled per pixel into a 256x256 bitmap (yaw rotation only, 120 s per turn like iOS, drag to turn, auto-spin resumes on release). Pins are normal composables placed over the sphere (flag image or 2-letter fallback), hidden when on the far side. Chosen because it has no engine dependency and only uses stable Compose/Android bitmap APIs.

## Differences from iOS (and why)
- Country list got a search field (debounced `name=` query) and a grid/map/globe switch, as requested; iOS CountryListView has neither (its map is reached from the aircraft detail). Map/globe modes load every country (pages of 100, sequentially, 300 ms apart) because pins need coordinates; the search then filters the pins client side.
- Country detail: iOS locks every equipment card for free users (`isLocked = !isPremiumUser`, a TODO placeholder). The row has no `is_vip`, so Android never locks there; the aircraft detail handles the 403.
- Manufacturer equipment cards use the placeholder image: iOS tries `national_origin` as an image URL (a bug) and the row has no thumbnail.
- Globe: no vertical tilt (iOS orbit turntable allows it), no pinch zoom; pins are flag circles from the API flag, not billboard planes. Flat map markers are default Google markers, not flag images and not the pulsing ring.
- Callout is a bottom panel (dark translucent; no real blur) with the same texts "Aircraft: N" / "More Info".
- iOS FeaturedView (Home carousels) and WW2ListView (commented out) were not ported as screens: Home owns its carousels. `FeaturedCard` is available for them. `ManufacturerKeyListView` is unused in iOS ("not currently use") and not ported.
- Back buttons: top bar with arrow on list screens; round dark overlay button on the detail covers (iOS toolbar button).
- Detail description font is Sailec (iOS uses Futura Light, which is not bundled).

## TODOs
- No `@Preview`/`Fixtures.kt` yet (composables read `LocalAppContainer` through `TGImage`, which needs a container in previews).
- Flags on the flat map markers (needs bitmap descriptors from Coil).
- Show the earth with a night-side shade / stars if wanted.
- `GeoFlag` prefers the API image; `TGImage` cannot load `file:///android_asset` URLs (MediaUrlResolver accepts http/https only), so bundled flag SVGs are loaded directly with Coil `AsyncImage`.

## Backend problems noticed
- `country/` list uses `fields='__all__'` plus `aircraft_list` and `manufacturers` for every row: heavy pages (page_size 100 for the map is several MB). A light list serializer (id, name, code, flag, lat, long, aircraft_count) would help.
- `manufacturer/{id}` `aircraft_list` rows have no `thumbnail` and no `is_vip`; country `aircraft_list` rows have no `is_vip`.
- Country list is ordered by `-aircraft_count` only (no tie breaker), so pages can repeat/skip rows when counts tie.
- `lat`/`long` are decimal strings and can be null (countries with no coordinates get no pin).
- `air_force_founded` is a DateField, shown as returned (`YYYY-MM-DD`).

## Unverified
- `compose-check` type-checks everything except `MapUi.kt` (ANDROID-ONLY, reviewed by eye against maps-compose 4.x and Compose APIs; not compiled).
- Nothing was run: layouts, globe performance (about 65k pixels per frame on the main thread), drag/spin behavior, Maps key setup, Coil SVG flags and the real API shapes are untested on a device.
- Insets: screens use `statusBarsPadding()`; behavior under edge-to-edge not seen.
