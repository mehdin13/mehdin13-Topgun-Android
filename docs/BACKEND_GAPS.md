# Backend gaps (ncodestudio/topgun-backend)

Found while porting to Android (2026-10-22), from reading the Django source and the iOS app. Do not fix from the Android repo; send to the backend owner. Severity: **S** security, **B** bug, **C** contract mismatch, **P** performance / platform need.

## Security
1. **S – Social login trusts the client.** `POST users/social_network_register/` creates or logs in any account from a bare `email`; no Google/Apple ID token is verified. Anyone who knows an email can get that user's token. Needed: accept an ID token and verify it server-side (Google `tokeninfo`/JWKS, Apple JWKS), derive the email from it.
2. **S – `make_vip` is unauthenticated and unverified.** `POST users/make_vip/` has no permission class and trusts `user_id` + `vip_days_remaining` from the app, so anyone can grant themselves Pro. Needed: remove it; rely on the RevenueCat webhook (secured by bearer key) or verify the purchase server-side.
3. **S – Throwaway account tokens are unlimited-lifetime until 180 days**, `logout` deletes the single token (all devices logged out together). Consider per-device tokens.

## Bugs
4. **B – First Google sign-in likely fails with 500.** `SocialNetworkLoginAPIView` calls `user.profile.badge.add(...)` but the field is `Profile.badges`. The same typo is in `UserRegistrationWithEmailAPIView`. The `transaction.atomic` block rolls back, so new users cannot register.
5. **B – `make_vip` NameError** when called with `user_id` only: the VIP branch uses `user` which is only defined in the email branch.
6. **B – RevenueCat webhook.** `CANCELLATION` only acts `if not profile.is_vip` (a no-op for subscribed users), `EXPIRATION` is not handled, and branches that do not match return `None` (500). Needed: set `is_vip=False` on `EXPIRATION`/`CANCELLATION` once the period ends; always return a response.
7. **B – Favorites list reports `is_favorite=false`** for every item (`GroupedFavoriteSerializer` runs `AircraftSerializer` without `request` in context). Android treats the whole list as favorites.
8. **B – Comments.** `CommentViewSet.create` has no permission check (anonymous users crash on `user=request.user`), and the aircraft detail embeds `AircraftComment` while the apps post/list the older `Comment` model (two unrelated comment systems). `AircraftLikeViewSet`/`AircraftCommentViewSet` define no `queryset`, so list/retrieve fail.
9. **B – `CategoryViewSet.get_aircrafts_by_category`** uses `request.user.profile.region` and crashes for anonymous users.
10. **B – Rating badge counts** use `hasattr(globals(), "Rating")`, which is always false, so rating badges never count.

## Contract mismatches (iOS and Android send fields the backend ignores)
11. **C – Filters.** `aircraft_filters` ignores `country_of_origin`, `introduction_year`, `electronic_warfare` and `range`, and expects `vip_status` = `vip` / `free` (the app sends `true`). Filtering by those controls does nothing. `GET aircraft/` also reads filters from `request.data` (works only because bodies are empty on GET).
12. **C – `lang`/`region`.** `Profile.region` defaults to `'us'`, which is not `'en'`, so every signed-in request tries to machine-translate every string field into language code `us` (swallowed exceptions, one network call per field). Slow lists for all signed-in users.
13. **C – Types.** `price_million`, `autonomy_hours` and `lat`/`long` are strings (DRF Decimal); `number_built`/`produced` are text but sometimes numeric in data. The Android models accept either.
14. **C – `Aircraft.is_vip` defaults to `True`**, so every new aircraft is Pro-only until edited.

## Platform needs
15. **P – 3D models are `.usdz`** (`file_3D`). Android (SceneView/ARCore, Filament) loads glTF/GLB only. Needed: a `file_3D_glb` (or per-platform) field, ideally Draco/meshopt compressed, with sizes.
16. **P – Account deletion.** Google Play requires an in-app and web way to delete the account and its data. There is no `DELETE users/me/` endpoint.
17. **P – Push notifications.** The app registers with Firebase, but the backend has no device-token model/endpoint (the logout view has commented-out APNs code).
18. **P – Throttle is 4 requests/second** per user and per IP for every endpoint. The home screen fires several calls at once; shared NATs (mobile carriers) share the anonymous bucket. Consider a higher limit for reads or scoped throttles.
19. **P – Social login for Android needs the Google *web* client ID** to be configured as an allowed audience once item 1 is fixed (the iOS client ID cannot be reused).
