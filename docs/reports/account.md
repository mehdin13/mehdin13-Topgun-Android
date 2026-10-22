# Account group report: Favorites, Settings (+ theme), Blog, Badges

`tools/compose-check/check.sh` prints `compose-check: OK` with these files. Nothing was run on a device or emulator.

## Ported
- **Favorites** (`features/favorites/FavoritesScreen.kt`, `FavoritesViewModel.kt`)
  - "Favorite" title and a grid/list toggle. The grid is adaptive with a 160dp minimum cell and 15dp gaps, and each card has a 180dp image, a two-line name and a heart. The list reuses `AircraftListCard`.
  - Data comes from `api.favorites()`. Every item is treated as favorite (`isFavorite=true`) because of the backend bug.
  - The heart removes the item right away, then calls `api.toggleFavorite`. If the request fails, the list reloads and a red message appears (tap it to dismiss). If the server answers "added", the toggle is repeated once.
  - VIP aircraft show the lock overlay unless the user is Pro. Tapping a locked one calls `onShowPaywall()` when signed in, otherwise `onShowSignIn()`.
  - There are signed-out (Sign In button), loading, error-with-retry and empty states. The empty state shows iOS's "No results found".
  - The list reloads each time the tab is shown (`LaunchedEffect`), like iOS `.task`. A failed refresh keeps the list that is already showing.
- **Settings** (`features/settings/SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsComponents.kt`, `ThemeScreen.kt`)
  - Header: avatar (80dp with white ring; the default avatar comes from `R.drawable.avatar`), first name in Futura Bold 18 (or "TopGun"), email, and a Sign In button when signed out.
  - General: notifications switch (`prefs` plus `platform.setNotificationsEnabled`), Blog, Badges, and Get Topgun Pro (`onShowPaywall`, or `onShowSignIn` when signed out). Get Pro is hidden for Pro users, as on iOS.
  - Restore Purchases uses `platform.restorePurchases()` then `user.applyProStatus`, with a snackbar for the result.
  - Preference: Language (English, read-only), and Clear Cache. Clear Cache shows "N files . size" from `platform.modelCacheInfo()` with the iOS size formatting, and a confirm dialog before `clearModelCache()`.
  - Rate! (Rate Us, Share with Friends), Support (Website, Support, Privacy Policy, Request feature), Theme, and Account (Instagram, Twitter, Logout with confirm via `user.signOut()`).
  - Texts, links and the support email address are copied from iOS. The Logout row only shows when signed in.
  - Appearance: "Change Theme" opens `ThemeScreen` in place (System / Light / Dark with a check mark), saved through `prefs.setAppearance`. Back goes through `BackHandler`.
- **Blog** (`features/blog/BlogScreen.kt`, `BlogViewModel.kt`)
  - Cards with a 200dp image, bold title, source logo, source name and "Read More". Tapping a card opens the detail in place: image, 30dp logo, source, the `sub_header` as headline, and the description.
  - Three gray placeholder cards while loading, and an error state with retry.
  - Pagination is new: `api.blogs(page)` loads the next page when the footer is reached, one request at a time, with a retry on error. iOS only loaded page 1.
- **Badges** (`features/badges/BadgesScreen.kt`, `BadgesViewModel.kt`, `BadgeDetail.kt`)
  - Badges come from `api.userInfo().badges`, shown in a 3-column grid with round images. Locked badges (`isHave=false`) are 50% opaque with a lock and cannot be opened. Earned ones are tappable.
  - The detail is a 300x400 card with a cover (the API `cover`, falling back to the bundled `back4`), the badge `icon`, and the name below it. Dragging gives the iOS parallax (background shifts 1/10, badge 1/5) and a tilt of up to 10 degrees. It resets on release.
- **Shared additions**
  - `designsystem/components/AccountTopBar.kt` (back button + title) and `FavoriteHeartButton.kt` (white 40dp circle, red heart, 48dp tap area).
  - `designsystem/TGAccountText.kt` (`TGText.largeTitle`, `TGText.detailTitle`).
  - Appended `TGAccountSize` and `TGAccountRadius` to `TGTokens.kt`, and `TGAccountColors` to `TGColors.kt`, each with the iOS source in comments.

## Differences from iOS, and why
- "Change App icon" row omitted: Android cannot switch the launcher icon easily.
- The WishKit "Request feature" board has no Android equivalent. The row opens an email to the support address with the subject "Feature request".
- The "Unit System" picker is not ported. It was not in the task, and `AppPreferences` has no unit-system preference.
- No profile photo picker or crop (iOS MediaPicker and CropViewController). The avatar is the server `avatar` or the default image.
- Share text says Google Play. The link assumes the listing uses `app.topgun.android`, which is unverified.
- Logout does not reset the intro flag. iOS also sets `isOnboarding=false`, which shows the intro again.
- Blog and Badges have a visible top bar with a back arrow. iOS hid the nav bar and relied on swipe-back.
- Blog gray text uses `textSecondary` instead of `TGBrand.gray`, which is too faint on a white card.
- The shimmer effect, the blurred circle in the badge detail, and the pop animation on the favorite heart are not ported.
- The badge title on iOS is the hard-coded "Aircrew Piolet". Android shows the API badge name.
- The blog detail image is a 200dp cropped box (iOS scales it to fit with a 200dp minimum). The image aspect ratio is unknown.
- Dialog and snackbar text uses the Material defaults and theme colors (`AlertDialog`, `Snackbar`), not custom styling.
- Icons are the nearest Material icons (for example `Verified` for `checkmark.seal.fill`, `WorkspacePremium` for `medal.fill`).

## Backend and API notes
- Favorites: `GroupedFavoriteSerializer` returns `is_favorite=false` for every item (already in `docs/BACKEND_GAPS.md` #7).
- `users/user_info/` returns all badges with `is_have`. I only read the serializer, not the view logic. Backend gap #4 (`badge.add`) and #10 (the rating badge) mean badges may never be awarded.
- Blog uses `page` and `page_size` like the other lists. `source_logo` and `image` are relative `/media/...` paths and are resolved by `TGImage`.

## TODOs
- Previews: none added. `TGImage` needs `LocalAppContainer`, and there is no preview fixture container yet.
- Real Play Store URL for sharing; confirm the support email and social handles (copied from iOS, marked TODO there).
- If the owner wants the intro to show again after logout, call `prefs.setIntroDone(false)` in `SettingsViewModel.signOut`.
- If the owner wants Appearance as its own route, add a route in the nav graph. It is currently an in-tab sub-screen because the `SettingsScreen` signature has no navigation callback for it.

## Unverified
- Only the compose-check type check ran. Layout, gestures (badge drag), snackbar placement above the floating tab bar, back handling, and dialogs are unseen on a device.
- `shadow(ambientColor, spotColor)` needs API 28+ to show colors; on API 26-27 it is a plain shadow.
- Real behavior of `platform.restorePurchases()`, `modelCacheInfo()`, `clearModelCache()`, `setNotificationsEnabled()`, `openRateApp()` depends on the `// ANDROID-ONLY` implementations, which I did not read or check.
- The 403 / 429 paths on Favorites and Blog are covered only through `ApiError.userMessage`.
- `Color.Black` / `Color.White` are used for the VIP lock overlay in the Favorites grid (same as `AircraftListCard`, not tokenized). The 0.3 / 0.4 / 0.5 / 0.6 alpha values are literals.
