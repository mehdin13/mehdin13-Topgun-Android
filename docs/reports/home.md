# Home screen port report

Verified: `tools/compose-check/check.sh` prints `compose-check: OK` (type-check only; nothing was run on a device or emulator, and no screenshots were compared with iOS).

## Ported (iOS source -> Android)
- `HomeView.swift` -> `features/home/HomeScreen.kt` (`HomeScreen` keeps its signature; `HomeContent` is the stateless layout used by previews) and `HomeSections.kt` (header, search row, Welcome cards, category chips, list content, row with context menu).
- `HomeViewModel.swift` -> `HomeViewModel.kt`. One `HomeSource` at a time (All, Category, Search, Filter) with `Load More` for each; requests are sequential, a new source cancels the previous request, Load More is ignored while loading. Page size 10 as iOS. Uses `api.aircraft`, `aircraftByCategory`, `searchAircraft`, `filterAircraft`, `toggleFavorite`.
- `FilterView.swift` -> `FilterState.kt` (options, `FilterState`, `SpeedCategory`, `FilterViewModel`) and `FilterSheet.kt` (ModalBottomSheet with Reset / Filter / Done, all sections, same option lists, 78 operating countries from `all_countries_complete.json` with flag SVGs).
- `FeaturedSectionCardView` / `FeaturedSectionItem` -> `designsystem/components/FeaturedSectionCard.kt` + gradients in `TGHomeColors.featureGradients`. Cards open `onOpenCountries`, `onOpenManufacturers`, `onOpenList(name)` ("Topgun Selected", "Top WWII", "Cold War Icons", "Stealth Dominance", "Carrier Borne", "Best Helicopter").
- Category chips -> `CategoryTile.kt` (tap again clears). Search -> `TGSearchField.kt` (search on submit, clear button, Cancel while editing). Load More -> `GradientLoadMoreButton.kt`.
- VIP lock: `isVip && !user.isPremium` shows the lock overlay; tap calls `onShowPaywall()` if signed in else `onShowSignIn()`.
- Long press (context menu): Add/Remove favorite + Share. Favorite needs sign-in (else `onShowSignIn()`), then Snackbar "Added To Favorite" / "Remove From Favorite" and the row updates. Share uses `platform.share(name + thumbnail URL)`.
- Pull to refresh (`PullToRefreshBox`): clears search and category, vibrates, reloads page 1.
- Tab bar hides on scroll down, shows on scroll up and at the top (`onSetTabBarVisible`), threshold from iOS (10).
- Tokens appended (not edited): `TGHomeColors`, `TGHomeSpace`, `TGHomeSize`, `TGHomeRadius` (TGColors.kt / TGTokens.kt), `TGHomeText` (TGTypography.kt). `AircraftListCard` got two optional, defaulted params (`onLongClick`, `onLongClickLabel`) and now uses `combinedClickable`; existing callers are unaffected.
- `Fixtures.kt` has 6 `@Preview`s (loaded+category, Pro+search, loading, error, no results, filter sheet) and a network-free `AppContainer` for them.

## Differences from iOS (and why)
- Errors show `ApiError.userMessage` (project rule) instead of the fixed "Oops! Something went wrong". Empty/Search texts match iOS ("No aircraft available", "No results found"), but use the shared `EmptyState` (no circle, no "Try refreshing..." subtext, button says "Try again").
- Search results are paged with Load More (iOS shows only the first page). `hasMore` comes from `next != null` for every source (iOS used `count == pageSize` for the plain list).
- Starting a search, category or filter replaces the previous source and clears the search text/category highlight, so the UI never shows a category chip selected while a filter list is displayed (iOS leaves it highlighted).
- An empty filter (after Reset + Done) shows the plain list instead of posting an empty filter.
- Filter selections are kept while the Home screen lives (iOS recreates the sheet blank every time).
- Filter sliders: iOS bound the Price and Range sliders to the same variable and started them at 100/1000/1000, which are off or at arbitrary points of their tracks. Here Price (10..300 M, "Up to"), Range (0..5000 km) and Introduction Year (1950..2040, "From") are independent and start at "Any" (far end / 1950); "any" sends nothing. `AircraftFilter` treats exactly 100 and 1000 as "unset", so those two values are sent as 100.5/1000.5 (which `toInt()`s to the same number).
- Radio groups (type, status, speed, hardpoints, manufacturer, country of origin) can be un-selected by tapping again (iOS needs Reset). "United State" typo fixed to "United States" and the South Korea flag code (iOS used "SK") to `kr`.
- The locked VIP switch in the filter opens the paywall / sign-in (iOS: a dead disabled switch).
- Category tile size is 60 dp on all devices (iOS uses 120 on iPad). No image shadow on category icons. SF Symbols are replaced by the closest Material icons (e.g. gearshape.2.fill -> Settings, shield.lefthalf.filled -> Security). The iOS "Cow" ASCII constant and unused `GenerationBadge`/`Generation` views were not ported.
- The system "large title / rounded" fonts use the existing `TGText` substitutes; `TGText.title` is 22 sp (iOS `.title` is 28).
- Share sends text only (name + image URL); iOS also attaches the image file.
- The Load More button's minimum height is 48 dp (iOS 44) per the project's tap-target rule.

## Not verified
- Nothing ran on Android: layout, spacing, scrolling behaviour (tab bar hide thresholds), the `PullToRefreshBox`/`ModalBottomSheet` look, Coil SVG flags and the dropdown menu position are untested. The checker compiles against Compose Multiplatform 1.7.3 material3, while the app uses the BOM 2024.12.01 material3 (1.3.x); `PullToRefreshBox`, `ModalBottomSheet`, `combinedClickable` exist in both, but an `@OptIn` may be needed on `combinedClickable` for foundation 1.7 (already added).
- No live API calls were made; request shapes follow `TopGunApi`.
- No unit tests were added (`:app` tests cannot run here). `FilterState.toFilter` and `HomeViewModel` are plain Kotlin and are good first test targets (needs a fake `ApiClientContract`).

## Backend problems noticed (read-only; for BACKEND_GAPS.md)
- `hardpoints` is parsed with `int(value)` in `apps/aircrafts/utils.py`, but the apps send "<5 Hardpoints", "5-10 Hardpoints", ">10 Hardpoints", so the filter is silently ignored. Same for `crew_size` semantics (1 pilot is never sent).
- `speed` is cast to float and compared with the model's speed field; the unit stored there is not documented, while iOS/Android send km/h converted from mph ranges. Results may be wrong if the field holds mph or mach.
- Already known and kept: `country_of_origin`, `introduction_year`, `electronic_warfare`, `range` are ignored; `vip_status` must be "vip"/"free".
- `generation` options "3th"/"2th" (iOS typos) are sent as is and match only if the database has the same strings.
- Searching creates/updates a `SearchQuery` row per request (writes on a GET).
