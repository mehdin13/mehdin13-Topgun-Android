# Entry flow port: Intro, Welcome story, Splash, Sign-in, Paywall

## Ported (iOS source -> Android)
| iOS | Android |
|---|---|
| `View/Intro/Intro.swift` (`Intro`, `IntroView`), `Model/PageIntro.swift` | `features/intro/IntroScreen.kt`, `IntroViewModel.kt`, `IntroPages.kt` (same 4 pages, copy, images `page_1..4`, slide-in/out springs, back chevron, "Next"/"Continue") |
| `CustomIndicatorView.swift` | `designsystem/components/EntryPageIndicator.kt` |
| capsule buttons | `designsystem/components/EntryPillButton.kt` |
| `View/WelcomeAppStory/*` | `features/intro/WelcomeStoryScreen.kt`, `WelcomeStoryViewModel.kt` (pager + 3 s auto-advance + progress bars). NOT wired into navigation |
| `SplashView.swift` | `features/splash/SplashScreen.kt` (black, Lottie `splashlogo` 150 dp, once) |
| `SignInView.swift`, `SocialLoginButton` | `features/auth/SignInScreen.kt`, `SignInViewModel.kt`, `components/SocialLoginButton.kt` |
| `HomeView` paywall sheet + `handlePurchaseCompletion` + `calculateSubscriptionDays` | `features/paywall/PaywallScreen.kt`, `PaywallViewModel.kt`, `platform/BillingHelper.kt` (`proStatus`, `daysFor`), `platform/ui/PaywallUi.kt` |
| `Top_GunApp` (Firebase/RevenueCat setup, push) | `platform/BillingHelper.configureIfNeeded` (lazy), `platform/TopGunMessagingService.kt` |
| Google sign-in (`UserAuthModel`) | `platform/GoogleSignInHelper.kt`; the post-login work (POST `social_network_register`, save token/e-mail/first name/is_vip) already lives in `UserRepository.signInWithGoogle` |
| Lottie view | `platform/ui/LottieUi.kt` |

Tokens added (comments name the iOS source): `TGEntry` in `TGTokens.kt`, `TGEntryColors` (black/white) in `TGColors.kt`. Docs: `docs/SETUP.md`, `docs/reports/entry-manifest-needs.md`.

## Differences from iOS, and why
- **Google only** on the sign-in screen (Apple needs a web flow on Android). Added a close button (iOS dismisses the sheet by swipe) and an inline friendly error line; the button shows a spinner while signing in. Cancelled picker does nothing.
- Privacy / Terms links open the system browser via `platform.openUrl` (iOS shows an in-app Safari sheet) and use the same placeholder URL `https://www.Topgunapp.com/` for both, as iOS does (marked TODO there). The iOS file also has a bug where both links toggle the same sheet flag; not reproduced.
- **Intro**: iOS dark mode uses black buttons on a dark background; here dark mode uses the accent color for the capsule and the active dot. Email/password fields in iOS are commented out; `CustomTextField.swift` is therefore not ported. Return of the keyboard-offset code is not needed.
- **Splash** finishes when the Lottie ends (about 2.6 s) or at 3 s, whichever is first; iOS always waits 3 s. The 3 s timer also covers a Lottie that fails to load. iOS also calls `fetchUserInfo` on launch: already done in `TopGunRoot` (`user.refresh()`).
- **Welcome story**: the iOS file is a template leftover (Indonesian "Skuy" copy, images `onboard1..4` that do not exist in the catalog, and two buttons that only dismiss) and the iOS app never shows it. I kept its structure and used the Top Gun intro art with short English copy and a single "Continue". It is available as `WelcomeStoryScreen(onContinue)` but not routed. Delete it if you do not want it.
- **Pro days**: the exact iOS ids are matched case-insensitively and without the `:basePlan` suffix, then fallback by name (`6month`, `12month|annual|yearly`, `lifetime`). Google Play ids must be lowercase, so the iOS ids cannot be reused as is. iOS reads `activeSubscriptions.first`, which cannot see the lifetime (non-subscription) purchase; Android reads the "Pro" entitlement's product first. Unknown product -> 0 days (same as iOS), which `makeVip` is then called with.
- Restore from inside the paywall (`onRestoreCompleted`) is treated like a purchase, so the Pro flag and `make_vip` are also applied for restores.
- Push: basic notification and channel only; no FCM token upload (no backend endpoint).

## TODO
- Request `POST_NOTIFICATIONS` at runtime (iOS asks at launch). Nobody does yet.
- Success toast after a purchase (iOS `toastSucess`): no toast component exists in the design system.
- Final Privacy Policy / Terms URLs. Real keys in `local.properties` (see `docs/SETUP.md`).
- Subscribe/unsubscribe FCM topic in `AndroidPlatform.setNotificationsEnabled` (not my file).
- Play products + RevenueCat Android app and a current offering with a paywall.

## Backend problems noticed (already in BACKEND_GAPS.md: #1, #2, #4, #17, #19)
- Social login accepts a bare e-mail, so the app cannot send an ID token even though Credential Manager provides one. Once the backend verifies tokens, `UserRepository`/`TopGunApi.socialLogin` must send it (GoogleAccount does not carry it yet).
- First sign-in of a new user probably 500s (#4), so the first Android sign-in may show "Something went wrong on our side".
- `make_vip` is unauthenticated (#2); the day count is client-supplied.

## Verification
- `tools/compose-check` (JVM type-check): my commons files compile; the last runs only reported errors in other workers' files (settings, details). Re-run to confirm after they finish.
- NOT verified: all `// ANDROID-ONLY` files (`GoogleSignInHelper`, `BillingHelper`, `LottieUi`, `PaywallUi`, `TopGunMessagingService`). They are written against the RevenueCat 8.10.1 (`awaitCustomerInfo`, `awaitRestore`, `PaywallOptions.Builder(dismissRequest)`, `setShouldDisplayDismissButton`, `PaywallListener`), Credential Manager 1.3.0 / googleid 1.1.1 and Lottie 6.6.2 APIs from memory, never compiled. Likeliest compile risks: RevenueCat paywall option names, `Purchases.isConfigured`.
- No screen has been run on a device or emulator: animations (spring constants converted from iOS response 0.8), inset handling (`systemBarsPadding`, edge-to-edge) and layout on small phones are unchecked. The Google sheet needs a foreground Activity; `GoogleSignInHelper` tracks it with lifecycle callbacks registered in its constructor.
- Dark-mode contrast of the capsule/dots and the white Google button on the dark background is a judgment call.
